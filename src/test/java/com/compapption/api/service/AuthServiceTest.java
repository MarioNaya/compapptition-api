package com.compapption.api.service;

import com.compapption.api.config.JwtService;
import com.compapption.api.entity.PasswordResetToken;
import com.compapption.api.entity.RefreshToken;
import com.compapption.api.entity.Usuario;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.repository.*;
import com.compapption.api.dto.auth.RegistroRequest;
import com.compapption.api.dto.auth.LoginRequest;
import com.compapption.api.dto.auth.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private EmailService emailService;

    @InjectMocks private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L).username("mario").email("mario@test.com")
                .password("hashed").activo(true)
                .build();
    }

    // =========================================================
    // registro() — validaciones de duplicados
    // =========================================================

    @Test
    void registro_usernameEnUso_lanzaBadRequest() {
        when(usuarioRepository.existsByUsername("mario")).thenReturn(true);

        RegistroRequest req = new RegistroRequest();
        req.setUsername("mario");
        req.setEmail("otro@test.com");

        assertThatThrownBy(() -> authService.registro(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("usuario");
    }

    @Test
    void registro_emailEnUso_lanzaBadRequest() {
        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("mario@test.com")).thenReturn(true);

        RegistroRequest req = new RegistroRequest();
        req.setUsername("nuevo");
        req.setEmail("mario@test.com");

        assertThatThrownBy(() -> authService.registro(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("email");
    }

    // =========================================================
    // login() — cuenta desactivada
    // =========================================================

    @Test
    void login_cuentaDesactivada_lanzaUnauthorized() {
        Usuario inactivo = Usuario.builder().id(2L).username("inactivo").activo(false).build();
        when(usuarioRepository.findByUsernameOrEmail("inactivo", "inactivo"))
                .thenReturn(Optional.of(inactivo));

        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("inactivo");
        req.setPassword("pass");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("desactivada");
    }

    // =========================================================
    // refreshToken() — token revocado y expirado
    // =========================================================

    @Test
    void refreshToken_tokenRevocado_lanzaUnauthorized() {
        RefreshToken rt = RefreshToken.builder()
                .token("tok").revocado(true)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenWithUsuario("tok")).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refreshToken("tok"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("revocado");
    }

    @Test
    void refreshToken_tokenExpirado_lanzaUnauthorized() {
        RefreshToken rt = RefreshToken.builder()
                .token("tok").revocado(false)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().minusDays(1)) // expirado
                .build();

        when(refreshTokenRepository.findByTokenWithUsuario("tok")).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refreshToken("tok"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expirado");
    }

    // =========================================================
    // resetPassword() — token inválido, usado, expirado
    // =========================================================

    @Test
    void resetPassword_tokenNoExiste_lanzaBadRequest() {
        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("bad-token");
        req.setNuevaPassword("nueva");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void resetPassword_tokenYaUsado_lanzaBadRequest() {
        PasswordResetToken prt = PasswordResetToken.builder()
                .token("tok").usado(true)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusHours(1))
                .build();

        when(passwordResetTokenRepository.findByToken("tok")).thenReturn(Optional.of(prt));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("tok");
        req.setNuevaPassword("nueva");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("utilizado");
    }

    @Test
    void resetPassword_tokenExpirado_lanzaBadRequest() {
        PasswordResetToken prt = PasswordResetToken.builder()
                .token("tok").usado(false)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().minusHours(1)) // expirado
                .build();

        when(passwordResetTokenRepository.findByToken("tok")).thenReturn(Optional.of(prt));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("tok");
        req.setNuevaPassword("nueva");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expirado");
    }
}
