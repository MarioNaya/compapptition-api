package com.compapption.api.controller;

import com.compapption.api.dto.auth.*;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * Controlador REST para autenticación y gestión de sesión. Expone endpoints bajo la ruta base /auth.
 * Gestiona el registro de nuevos usuarios, inicio de sesión, refresco de tokens JWT,
 * cierre de sesión y recuperación/restablecimiento de contraseña.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /** Path al que se restringe la cookie del refresh token. Cubre los dos
     * endpoints que la consumen ({@code /auth/refresh} y {@code /auth/logout})
     * sin filtrarse al resto de la API ({@code /usuarios}, {@code /equipos},
     * etc.). Si se restringe a {@code /auth/refresh} el navegador deja de
     * enviarla en logout y la sesión queda viva 7 días en BD (cierra S-33). */
    private static final String REFRESH_COOKIE_PATH = "/auth";
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    /**
     * POST /auth/registro — registra un nuevo usuario en el sistema.
     * Crea la cuenta, genera los tokens JWT y establece la cookie HTTP-only del refresh token.
     *
     * @param request datos de registro del nuevo usuario (nombre, email, password)
     * @param response respuesta HTTP para adjuntar la cookie del refresh token
     * @return ResponseEntity con el AuthResponse que contiene access token y datos del usuario
     */
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(
            @Valid @RequestBody RegistroRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.registro(request);
        addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(redactRefreshToken(authResponse));
    }

    /**
     * POST /auth/login — autentica a un usuario con sus credenciales.
     * Genera nuevos tokens JWT y establece la cookie HTTP-only del refresh token.
     *
     * @param request credenciales de acceso (email y password)
     * @param response respuesta HTTP para adjuntar la cookie del refresh token
     * @return ResponseEntity con el AuthResponse que contiene access token y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(redactRefreshToken(authResponse));
    }

    /**
     * POST /auth/refresh — renueva el access token usando el refresh token.
     * El refresh token se lee EXCLUSIVAMENTE de la cookie HttpOnly {@code refresh_token}
     * (cierra SF-4 y S-17). El cuerpo de la petición se ignora; el cliente debe
     * disparar la petición con {@code withCredentials: true}. Se aplica rotación:
     * el token antiguo se invalida y se emite uno nuevo.
     *
     * @param cookieRefreshToken refresh token leído de la cookie HTTP-only
     * @param response respuesta HTTP para actualizar la cookie con el nuevo refresh token
     * @return ResponseEntity con el AuthResponse que contiene el nuevo access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        if (cookieRefreshToken == null || cookieRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token no proporcionado");
        }
        AuthResponse authResponse = authService.refreshToken(cookieRefreshToken);

        // Actualizar cookie con el nuevo refresh token rotado
        addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(redactRefreshToken(authResponse));
    }

    /**
     * POST /auth/logout — cierra la sesión del usuario actual.
     * Revoca el refresh token en base de datos y elimina la cookie HTTP-only.
     *
     * @param cookieRefreshToken refresh token leído de la cookie HTTP-only
     * @param response respuesta HTTP para limpiar la cookie del refresh token
     * @return ResponseEntity con mensaje de confirmación de cierre de sesión
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        if (cookieRefreshToken != null) {
            authService.logout(cookieRefreshToken);
        }

        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    /**
     * POST /auth/recuperar-password — inicia el flujo de recuperación de contraseña.
     * Genera un token de un solo uso con validez de 24 horas y envía un email al usuario.
     * La respuesta es siempre la misma independientemente de si el email existe (seguridad).
     *
     * @param request objeto con el email del usuario que solicita recuperar la contraseña
     * @return ResponseEntity con mensaje genérico de confirmación
     */
    @PostMapping("/recuperar-password")
    public ResponseEntity<Map<String, String>> recuperarPassword(
            @Valid @RequestBody RecuperarPasswordRequest request) {
        authService.recuperarPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "Si el email existe, recibirás instrucciones para recuperar tu contraseña"
        ));
    }

    /**
     * POST /auth/reset-password — establece una nueva contraseña usando el token de recuperación.
     * Valida el token (no expirado, no usado) y actualiza la contraseña del usuario.
     *
     * @param request objeto con el token de recuperación y la nueva contraseña
     * @return ResponseEntity con mensaje de confirmación del cambio de contraseña
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    // -------------------------------------------------------------------------
    // Métodos privados
    // -------------------------------------------------------------------------

    /**
     * Emite la cookie del refresh token con todos los atributos endurecidos:
     * {@code HttpOnly} (no accesible desde JS), {@code Secure} (solo HTTPS),
     * {@code SameSite=Strict} (no se envía en navegaciones cross-site, mitiga CSRF),
     * y {@code Path=/auth/refresh} (restringe el envío al endpoint que la consume,
     * no se filtra al resto de la API). Cierra S-20.
     */
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Redacta el {@code refreshToken} del DTO de respuesta antes de serializarlo
     * al cliente. La cookie HttpOnly ya fue emitida con su valor real; quitarlo
     * del JSON evita que una extensión maliciosa con permiso {@code webRequest}
     * pueda exfiltrarlo desde {@code XMLHttpRequest.responseText} (cierra SF-21).
     */
    private AuthResponse redactRefreshToken(AuthResponse authResponse) {
        authResponse.setRefreshToken(null);
        return authResponse;
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
