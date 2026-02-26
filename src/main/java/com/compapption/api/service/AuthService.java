package com.compapption.api.service;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.config.JwtService;
import com.compapption.api.dto.auth.AuthResponse;
import com.compapption.api.entity.RefreshToken;
import com.compapption.api.entity.Usuario;
import com.compapption.api.entity.UsuarioRolCompeticion;
import com.compapption.api.repository.RefreshTokenRepository;
import com.compapption.api.repository.UsuarioRepository;
import com.compapption.api.repository.UsuarioRolCompeticionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;



    /// === HELPERS === ///

    private AuthResponse buildAuthResponse(Usuario usuario, List<UsuarioRolCompeticion> rolesCompeticion) {
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        String accessToken = jwtService.generateAccessToken(userDetails, rolesCompeticion);
        RefreshToken refreshToken = crearRefreshToken(usuario);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000) // Convertir a segundos
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
