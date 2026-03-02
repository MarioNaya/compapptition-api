package com.compapption.api.service;

import com.compapption.api.entity.Usuario;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.UsuarioMapper;
import com.compapption.api.repository.UsuarioRepository;
import com.compapption.api.request.usuario.UsuarioUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L).username("mario").email("mario@test.com")
                .password("hashed-pass").activo(true)
                .build();
    }

    // =========================================================
    // actualizar() — validaciones
    // =========================================================

    @Test
    void actualizar_usuarioNoExiste_lanzaResourceNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.actualizar(99L, new UsuarioUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizar_emailDuplicado_lanzaBadRequest() {
        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setEmail("otro@test.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndIdNot("otro@test.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.actualizar(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("email");
    }

    @Test
    void actualizar_emailNuevoUnico_actualizaEmail() {
        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setEmail("nuevo@test.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndIdNot("nuevo@test.com", 1L)).thenReturn(false);
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioMapper.toDTO(any())).thenReturn(null);

        usuarioService.actualizar(1L, req);

        assertThat(usuario.getEmail()).isEqualTo("nuevo@test.com");
    }

    // =========================================================
    // cambiarPassword() — validaciones
    // =========================================================

    @Test
    void cambiarPassword_usuarioNoExiste_lanzaResourceNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cambiarPassword(99L, "old", "new"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cambiarPassword_passwordActualIncorrecta_lanzaBadRequest() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrong", "hashed-pass")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, "wrong", "nueva"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("contraseña actual");
    }

    @Test
    void cambiarPassword_passwordCorrecta_actualizaPassword() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("correcta", "hashed-pass")).thenReturn(true);
        when(passwordEncoder.encode("nueva")).thenReturn("nueva-hash");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.cambiarPassword(1L, "correcta", "nueva");

        assertThat(usuario.getPassword()).isEqualTo("nueva-hash");
    }
}
