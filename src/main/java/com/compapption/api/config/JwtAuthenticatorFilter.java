package com.compapption.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filtro de autenticación JWT que se ejecuta una sola vez por petición HTTP.
 *
 * <p>Extiende {@link OncePerRequestFilter} y se registra en la cadena de seguridad
 * <em>antes</em> del filtro estándar {@code UsernamePasswordAuthenticationFilter}.
 *
 * <p>Estrategia de extracción del token:
 * <ol>
 *   <li>Cabecera {@code Authorization: Bearer <token>}.</li>
 *   <li>Cookie {@code access_token} (fallback para clientes que no pueden inyectar cabeceras).</li>
 * </ol>
 *
 * <p>Si el token es válido, construye un {@link CustomUserDetails} directamente desde los
 * claims del JWT —sin consultar la base de datos— y establece la autenticación en el
 * {@link org.springframework.security.core.context.SecurityContextHolder}.
 *
 * @author Mario
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticatorFilter extends OncePerRequestFilter {

    private final JwtService jwtService;


    /**
     * Lógica principal del filtro: extrae el JWT de la petición, lo valida y,
     * si es correcto, autentica al usuario en el {@code SecurityContext}.
     *
     * <p>Flujo:
     * <ol>
     *   <li>Extrae el token de la cabecera {@code Authorization} o de la cookie
     *       {@code access_token}.</li>
     *   <li>Si no hay token, delega directamente al siguiente filtro.</li>
     *   <li>Verifica la firma y la expiración del token con {@link JwtService#isTokenValid}.</li>
     *   <li>Si el token es válido y el contexto no tiene autenticación previa, construye un
     *       {@link CustomUserDetails} a partir de los claims (sin ir a BD) y lo establece
     *       en el {@code SecurityContextHolder}.</li>
     *   <li>Siempre invoca {@code filterChain.doFilter()} para continuar la cadena.</li>
     * </ol>
     *
     * @param request     la petición HTTP entrante.
     * @param response    la respuesta HTTP saliente.
     * @param filterChain la cadena de filtros restante.
     * @throws ServletException si ocurre un error en el procesamiento del servlet.
     * @throws IOException      si ocurre un error de E/S.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = extractTokenFromRequest(request);

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtService.isTokenValid(jwt)) {

                // Construir UserDetails directamente desde los claims del token.
                // El token ya ha sido verificado criptográficamente — no hace falta ir a BD.
                Long userId = jwtService.extractUserId(jwt);
                Boolean esAdminSistema = jwtService.extractEsAdminSistema(jwt);
                CustomUserDetails userDetails = new CustomUserDetails(userId, username,
                        esAdminSistema != null && esAdminSistema);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String fromCookie = Arrays.stream(cookies)
                    .filter(cookie -> "access_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
            if (fromCookie != null) return fromCookie;
        }

        // Fallback para endpoints SSE (EventSource no permite cabeceras custom):
        // acepta el token por query param `?token=...` solo en rutas /*/stream.
        String uri = request.getRequestURI();
        if (uri != null && uri.endsWith("/stream")) {
            String queryToken = request.getParameter("token");
            if (queryToken != null && !queryToken.isBlank()) {
                return queryToken;
            }
        }
        return null;
    }
}
