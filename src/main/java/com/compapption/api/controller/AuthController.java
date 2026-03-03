package com.compapption.api.controller;

import com.compapption.api.dto.auth.*;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(authResponse);
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
        return ResponseEntity.ok(authResponse);
    }

    /**
     * POST /auth/refresh — renueva el access token usando el refresh token.
     * Acepta el refresh token desde el cuerpo de la petición o desde la cookie HTTP-only.
     * Aplica rotación de refresh token: invalida el antiguo y emite uno nuevo.
     *
     * @param request cuerpo opcional con el refresh token
     * @param cookieRefreshToken refresh token leído de la cookie HTTP-only (alternativa al cuerpo)
     * @param response respuesta HTTP para actualizar la cookie con el nuevo refresh token
     * @return ResponseEntity con el AuthResponse que contiene el nuevo access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        String refreshToken = resolverRefreshToken(request, cookieRefreshToken);
        AuthResponse authResponse = authService.refreshToken(refreshToken);

        // Actualizar cookie con el nuevo refresh token rotado
        addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(authResponse);
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
            @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
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

    private String resolverRefreshToken(RefreshTokenRequest request, String cookieToken) {
        if (request != null && request.getRefreshToken() != null) {
            return request.getRefreshToken();
        }
        if (cookieToken != null) {
            return cookieToken;
        }
        throw new UnauthorizedException("Refresh token no proporcionado");
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
