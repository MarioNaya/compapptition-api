package com.compapption.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accesToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UsuarioInfoResponse usuario;

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
}
