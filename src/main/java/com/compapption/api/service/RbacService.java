package com.compapption.api.service;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.entity.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de control de acceso basado en roles (RBAC).
 * <p>
 * Centraliza las comprobaciones de autorización que se usan en las expresiones
 * {@code @PreAuthorize} de los controllers. Todas las operaciones son de solo
 * lectura. El administrador del sistema ({@code ROLE_ADMIN_SISTEMA}) siempre
 * tiene acceso sin importar el rol en la competición concreta.
 * </p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RbacService {

    private final UsuarioRolCompeticionService urcService;

    /**
     * Comprueba si el usuario autenticado tiene el rol global {@code ROLE_ADMIN_SISTEMA}.
     *
     * @param auth objeto de autenticación de Spring Security
     * @return {@code true} si el usuario es administrador del sistema
     */
    public boolean isAdminSistema(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN_SISTEMA"));
    }

    /**
     * Comprueba si el usuario es administrador de una competición concreta.
     * <p>
     * Los administradores del sistema superan esta comprobación automáticamente.
     * </p>
     *
     * @param competicionId identificador de la competición
     * @param auth          objeto de autenticación de Spring Security
     * @return {@code true} si el usuario tiene el rol {@code ADMIN_COMPETICION} en esa competición
     *         o es administrador del sistema
     */
    public boolean isAdminCompeticion(Long competicionId, Authentication auth) {
        if (isAdminSistema(auth)) return true;
        Long userId = extractUserId(auth);
        if (userId == null) return false;
        return urcService.tieneRol(userId, competicionId, Rol.RolNombre.ADMIN_COMPETICION);
    }

    /**
     * Comprueba si el usuario es administrador o manager de equipos en una competición.
     * <p>
     * Permite el acceso a funcionalidades restringidas a administradores y managers,
     * como la gestión de resultados o la configuración de equipos.
     * Los administradores del sistema superan esta comprobación automáticamente.
     * </p>
     *
     * @param competicionId identificador de la competición
     * @param auth          objeto de autenticación de Spring Security
     * @return {@code true} si el usuario tiene {@code ADMIN_COMPETICION} o {@code MANAGER_EQUIPO}
     *         en esa competición, o es administrador del sistema
     */
    public boolean isAdminOrManagerCompeticion(Long competicionId, Authentication auth) {
        if (isAdminSistema(auth)) return true;
        Long userId = extractUserId(auth);
        if (userId == null) return false;
        return urcService.tieneAlgunRol(userId, competicionId,
                List.of(Rol.RolNombre.ADMIN_COMPETICION, Rol.RolNombre.MANAGER_EQUIPO));
    }

    /**
     * Comprueba si el usuario tiene cualquier rol activo en la competición (es miembro).
     * <p>
     * Se utiliza para proteger recursos de consulta que solo deben ser visibles
     * para los participantes de la competición.
     * Los administradores del sistema superan esta comprobación automáticamente.
     * </p>
     *
     * @param competicionId identificador de la competición
     * @param auth          objeto de autenticación de Spring Security
     * @return {@code true} si el usuario tiene algún rol en la competición o es administrador del sistema
     */
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
