package com.compapption.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta de autenticación con los tokens JWT (access y refresh), información
 * del usuario autenticado y sus roles en cada competición, devuelto tras login o refresh.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UsuarioInfoResponse usuario;
    private List<CompeticionRolResponse> competiciones;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UsuarioInfoResponse {
        private Long id;
        private String username;
        private String email;
        private String nombre;
        private String apellidos;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompeticionRolResponse {
        private Long competicionId;
        private String competicionNombre;
        private String rol;
    }
}
