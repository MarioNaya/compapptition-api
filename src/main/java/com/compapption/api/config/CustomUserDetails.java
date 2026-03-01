package com.compapption.api.config;

import com.compapption.api.entity.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
