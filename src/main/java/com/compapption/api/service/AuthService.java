package com.compapption.api.service;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.config.JwtService;
import com.compapption.api.dto.auth.*;
import com.compapption.api.entity.PasswordResetToken;
import com.compapption.api.entity.RefreshToken;
import com.compapption.api.entity.Usuario;
import com.compapption.api.entity.UsuarioRolCompeticion;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.repository.PasswordResetTokenRepository;
import com.compapption.api.repository.RefreshTokenRepository;
import com.compapption.api.repository.UsuarioRepository;
import com.compapption.api.repository.UsuarioRolCompeticionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponse registro(RegistroRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("El nombre de usuario ya está en uso");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario registrado: {}", usuario.getUsername());

        // Usuario nuevo: sin competiciones todavía
        return buildAuthResponse(usuario, Collections.emptyList());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Localizar usuario (query simple, solo para comprobar activo y obtener username)
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new UnauthorizedException("La cuenta está desactivada");
        }

        // 2. Validar credenciales (Spring Security — llama a loadUserByUsername internamente)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getUsername(), request.getPassword())
        );

        // 3. Cargar contexto de competiciones en query separada
        List<UsuarioRolCompeticion> rolesCompeticion =
                usuarioRolCompeticionRepository.findByUsuarioIdWithRolesAndCompeticiones(usuario.getId());

        log.info("Usuario autenticado: {} — {} competiciones", usuario.getUsername(), rolesCompeticion.size());
        return buildAuthResponse(usuario, rolesCompeticion);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        // 1. Buscar en BD (no validar como JWT — ya no es un JWT)
        RefreshToken refreshToken = refreshTokenRepository.findByTokenWithUsuario(refreshTokenStr)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        // 2. Verificar que no está revocado
        if (refreshToken.isRevocado()) {
            throw new UnauthorizedException("Refresh token revocado");
        }

        // 3. Verificar expiración
        if (refreshToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expirado");
        }

        Usuario usuario = refreshToken.getUsuario();

        // 4. Cargar roles ACTUALES desde BD — clave del Nivel 2
        List<UsuarioRolCompeticion> rolesActuales =
                usuarioRolCompeticionRepository.findByUsuarioIdWithRolesAndCompeticiones(usuario.getId());

        // 5. Generar nuevo access token con roles actualizados
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        String nuevoAccessToken = jwtService.generateAccessToken(userDetails, rolesActuales);

        // 6. Rotación del refresh token (seguridad: invalida el anterior, emite uno nuevo)
        refreshToken.setRevocado(true);
        refreshTokenRepository.save(refreshToken);
        RefreshToken nuevoRefreshToken = crearRefreshToken(usuario);

        return AuthResponse.builder()
                .accessToken(nuevoAccessToken)
                .refreshToken(nuevoRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .usuario(mapToUsuarioInfo(usuario))
                .competiciones(mapToCompeticionRolResponse(rolesActuales))
                .build();
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByTokenWithUsuario(refreshTokenStr)
                .ifPresent(rt -> {
                    rt.setRevocado(true);
                    refreshTokenRepository.save(rt);
                    log.info("Refresh token revocado para usuario: {}", rt.getUsuario().getUsername());
                });
    }

    @Transactional
    public void recuperarPassword(RecuperarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElse(null);

        // Siempre responder con éxito para no revelar si el email existe
        if (usuario == null) {
            log.warn("Intento de recuperación para email no existente: {}", request.getEmail());
            return;
        }

        // Eliminar tokens anteriores del usuario para evitar acumulación
        passwordResetTokenRepository.deleteByUsuario(usuario);

        // Crear y persistir nuevo token con expiración de 24 horas
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusHours(24))
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.enviarEmailRecuperacion(usuario.getEmail(), usuario.getNombre(), token);
        log.info("Email de recuperación enviado a: {}", usuario.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Token de recuperación inválido"));

        if (Boolean.TRUE.equals(resetToken.getUsado())) {
            throw new BadRequestException("El token ya ha sido utilizado");
        }

        if (resetToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token de recuperación ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        passwordResetTokenRepository.save(resetToken);

        // Revocar todos los refresh tokens activos para forzar nuevo login
        refreshTokenRepository.revocarTodosPorUsuario(usuario);

        log.info("Contraseña restablecida para usuario: {}", usuario.getUsername());
    }

    // -------------------------------------------------------------------------
    // Métodos privados
    // -------------------------------------------------------------------------

    private AuthResponse buildAuthResponse(Usuario usuario, List<UsuarioRolCompeticion> rolesCompeticion) {
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        String accessToken = jwtService.generateAccessToken(userDetails, rolesCompeticion);
        RefreshToken refreshToken = crearRefreshToken(usuario);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .usuario(mapToUsuarioInfo(usuario))
                .competiciones(mapToCompeticionRolResponse(rolesCompeticion))
                .build();
    }

    private RefreshToken crearRefreshToken(Usuario usuario) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plus(
                        Duration.ofMillis(jwtService.getRefreshTokenExpiration())))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse.UsuarioInfoResponse mapToUsuarioInfo(Usuario usuario) {
        return AuthResponse.UsuarioInfoResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .build();
    }

    private List<AuthResponse.CompeticionRolResponse> mapToCompeticionRolResponse(
            List<UsuarioRolCompeticion> rolesCompeticion) {
        return rolesCompeticion.stream()
                .map(urc -> AuthResponse.CompeticionRolResponse.builder()
                        .competicionId(urc.getCompeticion().getId())
                        .competicionNombre(urc.getCompeticion().getNombre())
                        .rol(urc.getRol().getNombre().name())
                        .build())
                .toList();
    }
}
