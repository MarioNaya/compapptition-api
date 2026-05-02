package com.compapption.api.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro CORS de máxima prioridad que intercepta todas las peticiones antes de Spring Security.
 * Gestiona los preflights OPTIONS devolviendo 200 inmediatamente y añade las cabeceras
 * Access-Control-* en todas las respuestas cuyo origen esté en la lista permitida
 * (configurable vía {@code app.cors.allowed-origins}, separados por coma; cierra S-22).
 *
 * <p>En el perfil {@code prod} valida en el arranque que la lista no incluya
 * {@code localhost} ni esté vacía, abortando el contexto de Spring si se detecta
 * configuración insegura (cierra S-34: protege contra deploy con .env mal
 * configurado donde {@code FRONTEND_URL} se quedó apuntando a localhost).</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig extends OncePerRequestFilter {

    private final List<String> allowedOrigins;
    private final Environment environment;

    public CorsConfig(@Value("${app.cors.allowed-origins:http://localhost:4200}")
                      String allowedOriginsCsv,
                      Environment environment) {
        this.allowedOrigins = Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        this.environment = environment;
    }

    /**
     * Fail-fast en arranque: si el perfil activo es {@code prod}, los origins
     * no pueden estar vacíos ni contener {@code localhost} (ni el wildcard
     * {@code http://localhost:*}). Lanza {@link IllegalStateException} para
     * abortar el contexto si la configuración es insegura. Cierra S-34.
     */
    @PostConstruct
    void validateProductionOrigins() {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!isProd) {
            return;
        }
        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException(
                    "CorsConfig: en perfil 'prod', app.cors.allowed-origins no puede estar vacío. " +
                    "Define APP_CORS_ALLOWED_ORIGINS o app.frontend-url al dominio de producción.");
        }
        for (String origin : allowedOrigins) {
            String lowered = origin.toLowerCase();
            if (lowered.contains("localhost") || lowered.contains("127.0.0.1") || lowered.contains("0.0.0.0")) {
                throw new IllegalStateException(
                        "CorsConfig: en perfil 'prod', app.cors.allowed-origins no puede contener " +
                        "orígenes de loopback (encontrado: '" + origin + "'). " +
                        "Asegúrate de que FRONTEND_URL apunta al dominio público.");
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String origin = req.getHeader("Origin");
        if (origin != null && isOriginAllowed(origin)) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Access-Control-Allow-Credentials", "true");
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
            res.setHeader("Access-Control-Max-Age", "3600");
        }

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isOriginAllowed(String origin) {
        for (String allowed : allowedOrigins) {
            if (allowed.endsWith(":*")) {
                String prefix = allowed.substring(0, allowed.length() - 2);
                if (origin.startsWith(prefix + ":") || origin.equals(prefix)) {
                    return true;
                }
            } else if (allowed.equals(origin)) {
                return true;
            }
        }
        return false;
    }
}
