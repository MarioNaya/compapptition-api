package com.compapptition.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting in-memory por (IP × ruta sensible) basado en Bucket4j.
 * Cierra S-11: protege endpoints de autenticación, recuperación de contraseña y
 * el lookup de equipos por código de invitación contra brute force.
 *
 * <p>Política por defecto: 10 peticiones por minuto y por IP en cada ruta
 * sensible. Si se supera, devuelve {@code 429 Too Many Requests} con cabecera
 * {@code Retry-After} en segundos.</p>
 *
 * <p>Notas:
 * <ul>
 *   <li>Filtro registrado antes que Spring Security para que el rechazo no
 *       consuma autenticación ni logs de seguridad.</li>
 *   <li>El estado vive en un {@link ConcurrentHashMap} a nivel de proceso. Para
 *       un despliegue multi-instancia conviene migrar a Redis (Bucket4j ofrece
 *       backend Redis), pero la app actual corre en un solo nodo.</li>
 *   <li>La identidad del cliente se obtiene de {@code X-Forwarded-For} si
 *       existe (Hostinger reverse proxy), o el remoteAddr en su defecto.</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // tras CorsConfig pero antes de Spring Security
public class RateLimitFilter extends OncePerRequestFilter {

    /** Cubo único por (rutaPattern, identificadorCliente). */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Rutas vigiladas y su política asociada. POST salvo el lookup por código
     * (GET) que también es candidato a brute force del espacio de códigos.
     */
    private static final Map<String, Bandwidth> POLICIES = Map.of(
            "POST:/auth/login",                Bandwidth.simple(10, Duration.ofMinutes(1)),
            "POST:/auth/registro",             Bandwidth.simple(10, Duration.ofMinutes(1)),
            "POST:/auth/recuperar-password",   Bandwidth.simple(5,  Duration.ofMinutes(1)),
            "POST:/auth/reset-password",       Bandwidth.simple(10, Duration.ofMinutes(1)),
            "GET:/equipos/codigo/",            Bandwidth.simple(20, Duration.ofMinutes(1))
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String policyKey = matchPolicyKey(req);
        if (policyKey == null) {
            chain.doFilter(req, res);
            return;
        }

        String clientId = resolveClientId(req);
        Bucket bucket = buckets.computeIfAbsent(
                policyKey + "|" + clientId,
                k -> Bucket.builder().addLimit(POLICIES.get(policyKey)).build());

        var probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            chain.doFilter(req, res);
            return;
        }

        long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);
        res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write("{\"status\":429,\"message\":\"Demasiadas peticiones. Espera "
                + retryAfterSeconds + " segundos.\"}");
    }

    private String matchPolicyKey(HttpServletRequest req) {
        String method = req.getMethod();
        String uri = req.getRequestURI();
        for (String key : POLICIES.keySet()) {
            int colon = key.indexOf(':');
            String m = key.substring(0, colon);
            String pathPrefix = key.substring(colon + 1);
            if (m.equals(method) && (uri.equals(pathPrefix) || uri.startsWith(pathPrefix))) {
                return key;
            }
        }
        return null;
    }

    /**
     * Identifica al cliente. Detrás de un reverse proxy (Hostinger),
     * {@code request.getRemoteAddr()} sería siempre el del proxy y todos los
     * usuarios compartirían el mismo cubo. {@code X-Forwarded-For} expone la IP
     * real (toma el primer salto, que es el cliente).
     */
    private String resolveClientId(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return req.getRemoteAddr();
    }
}
