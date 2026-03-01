package com.compapption.api.service;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Rol;
import com.compapption.api.entity.Usuario;
import com.compapption.api.entity.UsuarioRolCompeticion;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.repository.RolRepository;
import com.compapption.api.repository.UsuarioRolCompeticionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioRolCompeticionService {

    private final RolRepository rolRepository;
    private final UsuarioRolCompeticionRepository urcRepository;

    // =========================================================================
    // Asignación de roles
    // =========================================================================

    /**
     * Asigna un rol genérico a un usuario en una competición.
     * Punto de extensión para futuros roles (ARBITRO, MANAGER_EQUIPO, JUGADOR…).
     */
    @Transactional
    public void asignarRol(Usuario usuario, Competicion competicion, Rol.RolNombre rolNombre) {
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", rolNombre.name()));

        UsuarioRolCompeticion urc = UsuarioRolCompeticion.builder()
                .usuario(usuario)
                .competicion(competicion)
                .rol(rol)
                .build();

        urcRepository.save(urc);
    }

    /** Conveniencia: asigna ADMIN_COMPETICION al creador al crear la competición. */
    @Transactional
    public void asignarRolAdminCompeticion(Usuario usuario, Competicion competicion) {
        asignarRol(usuario, competicion, Rol.RolNombre.ADMIN_COMPETICION);
    }

    // =========================================================================
    // Comprobaciones de rol (delegadas desde RbacService)
    // =========================================================================

    /** Devuelve true si el usuario tiene exactamente el rol indicado en la competición. */
    @Transactional(readOnly = true)
    public boolean tieneRol(Long usuarioId, Long competicionId, Rol.RolNombre rolNombre) {
        return urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombre(
                usuarioId, competicionId, rolNombre);
    }

    /** Devuelve true si el usuario tiene al menos uno de los roles indicados en la competición. */
    @Transactional(readOnly = true)
    public boolean tieneAlgunRol(Long usuarioId, Long competicionId, List<Rol.RolNombre> roles) {
        return urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombreIn(
                usuarioId, competicionId, roles);
    }

    /** Devuelve true si el usuario tiene cualquier rol en la competición (es miembro). */
    @Transactional(readOnly = true)
    public boolean esMiembro(Long usuarioId, Long competicionId) {
        return urcRepository.existsByUsuarioIdAndCompeticionId(usuarioId, competicionId);
    }

    // =========================================================================
    // Consultas de miembros (usadas desde CompeticionService)
    // =========================================================================

    /** Devuelve todos los registros URC de una competición (con usuario y rol). */
    @Transactional(readOnly = true)
    public List<UsuarioRolCompeticion> obtenerMiembros(Long competicionId) {
        return urcRepository.findByCompeticionId(competicionId);
    }

    /** Devuelve los roles que tiene un usuario concreto en una competición. */
    @Transactional(readOnly = true)
    public List<UsuarioRolCompeticion> obtenerRolesDeUsuario(Long usuarioId, Long competicionId) {
        return urcRepository.findByUsuarioIdAndCompeticionId(usuarioId, competicionId);
    }

    // =========================================================================
    // Revocación de roles (usada desde CompeticionService)
    // =========================================================================

    /** Elimina todos los roles de un usuario en una competición. */
    @Transactional
    public void revocarTodosLosRoles(Long usuarioId, Long competicionId) {
        urcRepository.deleteByUsuarioIdAndCompeticionId(usuarioId, competicionId);
    }
}
