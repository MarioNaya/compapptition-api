package com.compapption.api.config;

import com.compapption.api.entity.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementación de {@link UserDetails} que extiende los datos estándar de Spring Security
 * con campos propios de la aplicación.
 *
 * <p>Campos adicionales respecto a la interfaz base:
 * <ul>
 *   <li>{@code id} — identificador de la entidad {@code Usuario} en base de datos.</li>
 *   <li>{@code email} — dirección de correo electrónico del usuario.</li>
 *   <li>{@code esAdminSistema} — flag que indica si el usuario tiene privilegios de
 *       administrador global del sistema (rol {@code ROLE_ADMIN_SISTEMA}).</li>
 * </ul>
 *
 * <p>Dispone de dos constructores:
 * <ul>
 *   <li>{@link #CustomUserDetails(com.compapption.api.entity.Usuario)} — constructor completo
 *       para el flujo de login, carga todos los campos desde la entidad.</li>
 *   <li>{@link #CustomUserDetails(Long, String, boolean)} — constructor ligero para el filtro
 *       JWT, construye el principal desde los claims del token sin consultar la BD.</li>
 * </ul>
 *
 * @author Mario
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean esAdminSistema;

    public CustomUserDetails(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.password = usuario.getPassword();
        this.enabled = usuario.isActivo();
        this.esAdminSistema = Boolean.TRUE.equals(usuario.getEsAdminSistema());
    }

    /**
     * Constructor ligero para el JwtAuthenticationFilter.
     * Se construye desde los claims del token sin consultar la BD.
     */
    public CustomUserDetails(Long id, String username, boolean esAdminSistema) {
        this.id = id;
        this.username = username;
        this.email = null;
        this.password = null;
        this.enabled = true;
        this.esAdminSistema = esAdminSistema;
    }

    /**
     * Devuelve las autoridades (roles) asignadas al usuario en Spring Security.
     *
     * <p>Todos los usuarios reciben {@code ROLE_USER}. Si además {@code esAdminSistema}
     * es {@code true}, se añade {@code ROLE_ADMIN_SISTEMA}.
     * Los roles por competición ({@code ADMIN_COMPETICION}, {@code MANAGER_EQUIPO}, etc.)
     * se gestionan a través de {@code @PreAuthorize} con {@code RbacService} y no se
     * incluyen aquí para evitar recargar la BD en cada petición.
     *
     * @return colección de {@link GrantedAuthority} del usuario.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (esAdminSistema) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN_SISTEMA"));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
