package com.compapption.api.config;

import com.compapption.api.entity.UsuarioRolCompeticion;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para la generación y validación de tokens JWT usando la librería JJWT 0.12.6.
 *
 * <p>Gestiona dos tipos de tokens:
 * <ul>
 *   <li><b>Access token</b> — firmado con HS512, de corta duración, incluye {@code userId},
 *       {@code esAdminSistema} y la lista de competiciones con su rol.</li>
 *   <li><b>Refresh token</b> — almacenado en base de datos como entidad {@code RefreshToken};
 *       su tiempo de expiración se expone mediante {@link #getRefreshTokenExpiration()}.</li>
 * </ul>
 *
 * <p>La clave secreta y los tiempos de expiración se inyectan desde las propiedades
 * {@code jwt.secret}, {@code jwt.access-token-expiration} y {@code jwt.refresh-token-expiration}.
 *
 * @author Mario
 */
@Service
public class JwtService {

    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String secretKey;

    @org.springframework.beans.factory.annotation.Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Extrae el nombre de usuario (claim {@code sub}) del token JWT.
     *
     * @param token el token JWT del que extraer el subject.
     * @return el username almacenado en el claim {@code sub}.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el identificador de usuario del claim personalizado {@code userId}.
     *
     * @param token el token JWT.
     * @return el ID del usuario, o {@code null} si el claim no está presente.
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extrae el flag {@code esAdminSistema} del claim personalizado del mismo nombre.
     *
     * @param token el token JWT.
     * @return {@code true} si el usuario es administrador del sistema, {@code false} en caso
     *         contrario, o {@code null} si el claim no existe.
     */
    public Boolean extractEsAdminSistema(String token) {
        return extractClaim(token, claims -> claims.get("esAdminSistema", Boolean.class));
    }

    /**
     * Comprueba si el token JWT es válido, es decir, que no ha expirado.
     *
     * <p>La firma ya se verifica implícitamente al parsear el token en
     * {@link #extractAllClaims(String)}; este método añade la comprobación de expiración.
     *
     * @param token el token JWT a validar.
     * @return {@code true} si el token no ha expirado; {@code false} en caso contrario.
     */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    /**
     * Extrae un claim genérico del token aplicando la función {@code claimsResolver}.
     *
     * @param <T>            el tipo del valor del claim a extraer.
     * @param token          el token JWT.
     * @param claimsResolver función que obtiene el valor deseado del mapa de claims.
     * @return el valor del claim extraído.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera el access token JWT incluyendo el {@code userId}, el flag {@code esAdminSistema}
     * y la lista de competiciones con sus roles en claims personalizados.
     *
     * <p>Es el método principal que deben invocar {@code AuthService} y el flujo de refresh.
     * La duración del token está determinada por la propiedad
     * {@code jwt.access-token-expiration} (en milisegundos).
     *
     * @param userDetails       detalles del usuario autenticado; si es instancia de
     *                          {@link CustomUserDetails} se extraen {@code id} y
     *                          {@code esAdminSistema}.
     * @param rolesCompeticion  lista de relaciones usuario-rol-competición que se incluyen
     *                          como claim {@code competiciones}.
     * @return el token JWT firmado con HS512 listo para enviar al cliente.
     */
    public String generateAccessToken(UserDetails userDetails, List<UsuarioRolCompeticion> rolesCompeticion) {
        Map<String, Object> extraClaims = new HashMap<>();

        if (userDetails instanceof CustomUserDetails customDetails) {
            extraClaims.put("userId", customDetails.getId());
            extraClaims.put("esAdminSistema", customDetails.isEsAdminSistema());
        }

        List<Map<String, Object>> competicionesData = rolesCompeticion.stream()
                .map(urc -> Map.<String, Object>of(
                        "id", urc.getCompeticion().getId(),
                        "nombre", urc.getCompeticion().getNombre(),
                        "rol", urc.getRol().getNombre()
                ))
                .toList();
        extraClaims.put("competiciones", competicionesData);

        return buildToken(extraClaims, userDetails, accessTokenExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS512)
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Devuelve el tiempo de expiración del access token en milisegundos,
     * tal como está configurado en {@code jwt.access-token-expiration}.
     *
     * <p>Se utiliza en {@code AuthService} para incluir la expiración en la respuesta
     * de login y refresh, de modo que el cliente pueda gestionar la renovación del token.
     *
     * @return tiempo de expiración del access token en milisegundos.
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Devuelve el tiempo de expiración del refresh token en milisegundos,
     * tal como está configurado en {@code jwt.refresh-token-expiration}.
     *
     * <p>Se utiliza en {@code AuthService} para establecer la fecha de expiración de la
     * entidad {@code RefreshToken} que se persiste en base de datos.
     *
     * @return tiempo de expiración del refresh token en milisegundos.
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}

