package com.compapption.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XContentTypeOptionsHeaderWriter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración central de Spring Security para la aplicación.
 *
 * <p>Define la cadena de filtros de seguridad HTTP con las siguientes responsabilidades:
 * <ul>
 *   <li>Deshabilita CSRF (API REST sin estado).</li>
 *   <li>Configura CORS para los orígenes permitidos (frontend web y mobile).</li>
 *   <li>Establece la política de sesiones como {@code STATELESS} (JWT).</li>
 *   <li>Define las reglas de autorización por endpoint:
 *       {@code /auth/**} y determinados GET son públicos; el resto requiere autenticación.</li>
 *   <li>Registra el filtro {@link JwtAuthenticatorFilter} antes del filtro estándar de
 *       usuario/contraseña.</li>
 *   <li>Expone beans de {@code PasswordEncoder} (BCrypt), {@code AuthenticationProvider}
 *       (DAO) y {@code AuthenticationManager}.</li>
 * </ul>
 *
 * <p>CORS para preflights OPTIONS gestionado por {@link CorsConfig} — filtro de máxima prioridad.
 *
 * @author Mario
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticatorFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Lista CSV de origins CORS permitidos. En dev/default incluye localhost
     * y la URL del frontend; en prod debe sobreescribirse desde
     * application-prod.properties con solo el dominio de producción
     * (cierra S-22). Convención: separados por coma; los wildcards de puerto
     * usan la sintaxis {@code http://localhost:*}.
     */
    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOriginsCsv;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Headers de seguridad estándar (cierra S-25, S-28).
                // CSP base permisiva (compatible con Cloudinary y fonts) — el
                // frontend Angular añade además su propio meta http-equiv.
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .addHeaderWriter(new XContentTypeOptionsHeaderWriter())
                        .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "img-src 'self' data: https://res.cloudinary.com; " +
                                "font-src 'self' https://fonts.gstatic.com data:; " +
                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                "script-src 'self'; " +
                                // connect-src incluye los dominios del API y de dev local
                                // por coherencia con la CSP del frontend (cierra SF-24).
                                // Solo afecta a responses HTTP del backend, pero conviene
                                // mantener la política consistente entre lados.
                                "connect-src 'self' https://api.compapption.com http://localhost:8080; " +
                                "frame-ancestors 'none'; " +
                                "base-uri 'self'; " +
                                "form-action 'self'"
                        ))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight CORS — OPTIONS siempre permitido
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Endpoints públicos
                        .requestMatchers("/auth/**").permitAll()
                        // Endpoints públicos de consulta
                        .requestMatchers(HttpMethod.GET, "/clasificaciones/publicas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/deportes/**").permitAll()
                        // Resto de endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
