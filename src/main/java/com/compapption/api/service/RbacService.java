package com.compapption.api.service;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.entity.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RbacService {

    private final UsuarioRolCompeticionService urcService;

    public boolean isAdminSistema(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN_SISTEMA"));
    }

    public boolean isAdminCompeticion(Long competicionId, Authentication auth) {
        if (isAdminSistema(auth)) return true;
        Long userId = extractUserId(auth);
        if (userId == null) return false;
        return urcService.tieneRol(userId, competicionId, Rol.RolNombre.ADMIN_COMPETICION);
    }

    public boolean isAdminOrManagerCompeticion(Long competicionId, Authentication auth) {
        if (isAdminSistema(auth)) return true;
        Long userId = extractUserId(auth);
        if (userId == null) return false;
        return urcService.tieneAlgunRol(userId, competicionId,
                List.of(Rol.RolNombre.ADMIN_COMPETICION, Rol.RolNombre.MANAGER_EQUIPO));
    }

    public boolean isMiembroCompeticion(Long competicionId, Authentication auth) {
        if (isAdminSistema(auth)) return true;
        Long userId = extractUserId(auth);
        if (userId == null) return false;
        return urcService.esMiembro(userId, competicionId);
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails u)) return null;
        return u.getId();
    }
}
