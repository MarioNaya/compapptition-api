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

@Service
public class JwtService {

    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String secretKey;

    @org.springframework.beans.factory.annotation.Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public Boolean extractEsAdminSistema(String token) {
        return extractClaim(token, claims -> claims.get("esAdminSistema", Boolean.class));
    }

    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera el access token incluyendo el userId y el contexto de competiciones del usuario.
     * Es el metodo principal que deben usar AuthService y el flujo de refresh.
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

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}

