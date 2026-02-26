package com.compapption.api.config;

import com.compapption.api.entity.Usuario;
import com.compapption.api.entity.UsuarioRolCompeticion;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;

    public CustomUserDetails(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.password = usuario.getPassword();
        this.enabled = usuario.isActivo();
    }

    /**
     * Constructor ligero para el JwtAuthenticationFilter.
     * Se construye desde los claims del token sin consultar la BD.
     */
    public CustomUserDetails(Long id, String username) {
        this.id = id;
        this.username = username;
        this.email = null;
        this.password = null;
        this.enabled = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
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
