package com.compapption.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
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
 * @author Mario
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticatorFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Define la cadena de filtros de seguridad HTTP.
     *
     * <p>Reglas de autorización configuradas:
     * <ul>
     *   <li>{@code /auth/**} — acceso público (registro, login, refresh, recuperar contraseña).</li>
     *   <li>{@code GET /clasificaciones/publicas/**} — consulta pública de clasificaciones.</li>
     *   <li>{@code GET /deportes/**} — consulta pública del catálogo de deportes.</li>
     *   <li>Cualquier otro endpoint requiere autenticación JWT válida.</li>
     * </ul>
     *
     * @param http el objeto {@link HttpSecurity} proporcionado por Spring Security.
     * @return la {@link SecurityFilterChain} construida y lista para usar.
     * @throws Exception si ocurre algún error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Enpoints públicos
                        .requestMatchers("/auth/**").permitAll()
                        // Endpoints públicos de consulta
                        .requestMatchers(HttpMethod.GET, "/clasificaciones/publicas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/deportes/**").permitAll()
                        // Resto de endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura las reglas CORS globales de la aplicación.
     *
     * <p>Orígenes permitidos: la URL del frontend configurada en {@code app.frontend-url},
     * {@code http://localhost:4200} (Angular web en desarrollo) y
     * {@code http://localhost:8100} (Ionic mobile en desarrollo).
     * Métodos permitidos: GET, POST, PUT, PATCH, DELETE, OPTIONS.
     * Se expone la cabecera {@code Authorization} para que el cliente pueda leer el token.
     *
     * @return la fuente de configuración CORS aplicada a todos los paths ({@code /**}).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl, "http://localhost:4200", "http://localhost:8100"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Crea el proveedor de autenticación basado en base de datos.
     *
     * <p>Utiliza {@link CustomUserDetailsService} para cargar el usuario y
     * {@link BCryptPasswordEncoder} para verificar la contraseña.
     *
     * @return el {@link AuthenticationProvider} configurado.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el {@link AuthenticationManager} de la configuración de Spring Security.
     *
     * <p>Necesario para el flujo de login en {@code AuthService}, donde se autentica
     * explícitamente al usuario con sus credenciales.
     *
     * @param config la configuración de autenticación de Spring.
     * @return el {@link AuthenticationManager} listo para inyectar.
     * @throws Exception si no se puede obtener el manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Registra el codificador de contraseñas BCrypt como bean de Spring.
     *
     * <p>Se usa tanto en el registro de usuarios (para cifrar la contraseña)
     * como en la autenticación (para verificarla).
     *
     * @return una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
