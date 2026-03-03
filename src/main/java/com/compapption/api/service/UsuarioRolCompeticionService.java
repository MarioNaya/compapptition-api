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

/**
 * Servicio para la gestión de roles de usuario en el ámbito de una competición.
 * <p>
 * Permite asignar, consultar y revocar los roles que un usuario tiene en una
 * competición concreta ({@code ADMIN_COMPETICION}, {@code MANAGER_EQUIPO},
 * {@code JUGADOR}). Las comprobaciones booleanas son delegadas desde
 * {@link RbacService} para mantener separación de responsabilidades.
 * </p>
 *
 * @author Mario
 */
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
     * <p>
     * Punto de extensión para futuros roles ({@code ARBITRO}, {@code MANAGER_EQUIPO},
     * {@code JUGADOR}…). No comprueba si el rol ya existe; el servicio llamador
     * debe evitar duplicados si es necesario.
     * </p>
     *
     * @param usuario    entidad del usuario al que se asigna el rol
     * @param competicion entidad de la competición en la que se asigna el rol
     * @param rolNombre  nombre del rol a asignar
     * @throws com.compapption.api.exception.ResourceNotFoundException si el rol no existe en la BD
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

    /**
     * Método de conveniencia que asigna el rol {@code ADMIN_COMPETICION} al creador de la competición.
     *
     * @param usuario    entidad del usuario que será administrador
     * @param competicion entidad de la competición recién creada
     */
    @Transactional
    public void asignarRolAdminCompeticion(Usuario usuario, Competicion competicion) {
        asignarRol(usuario, competicion, Rol.RolNombre.ADMIN_COMPETICION);
    }

    // =========================================================================
    // Comprobaciones de rol (delegadas desde RbacService)
    // =========================================================================

    /**
     * Comprueba si el usuario tiene exactamente el rol indicado en la competición.
     *
     * @param usuarioId    identificador del usuario
     * @param competicionId identificador de la competición
     * @param rolNombre    rol que se desea comprobar
     * @return {@code true} si el usuario posee ese rol en la competición
     */
    @Transactional(readOnly = true)
    public boolean tieneRol(Long usuarioId, Long competicionId, Rol.RolNombre rolNombre) {
        return urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombre(
                usuarioId, competicionId, rolNombre);
    }

    /**
     * Comprueba si el usuario tiene al menos uno de los roles indicados en la competición.
     *
     * @param usuarioId    identificador del usuario
     * @param competicionId identificador de la competición
     * @param roles        lista de roles válidos para la comprobación
     * @return {@code true} si el usuario posee alguno de los roles indicados
     */
    @Transactional(readOnly = true)
    public boolean tieneAlgunRol(Long usuarioId, Long competicionId, List<Rol.RolNombre> roles) {
        return urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombreIn(
                usuarioId, competicionId, roles);
    }

    /**
     * Comprueba si el usuario tiene cualquier rol en la competición, es decir, si es miembro.
     *
     * @param usuarioId    identificador del usuario
     * @param competicionId identificador de la competición
     * @return {@code true} si el usuario tiene al menos un rol en la competición
     */
    @Transactional(readOnly = true)
    public boolean esMiembro(Long usuarioId, Long competicionId) {
        return urcRepository.existsByUsuarioIdAndCompeticionId(usuarioId, competicionId);
    }

    // =========================================================================
    // Consultas de miembros (usadas desde CompeticionService)
    // =========================================================================

    /**
     * Devuelve todos los registros {@code UsuarioRolCompeticion} de una competición,
     * incluyendo usuario y rol asociados.
     *
     * @param competicionId identificador de la competición
     * @return lista de registros de rol-usuario para la competición
     */
    @Transactional(readOnly = true)
    public List<UsuarioRolCompeticion> obtenerMiembros(Long competicionId) {
        return urcRepository.findByCompeticionId(competicionId);
    }

    /**
     * Devuelve todos los roles que tiene un usuario concreto en una competición.
     *
     * @param usuarioId    identificador del usuario
     * @param competicionId identificador de la competición
     * @return lista de registros rol-usuario para ese usuario y competición
     */
    @Transactional(readOnly = true)
    public List<UsuarioRolCompeticion> obtenerRolesDeUsuario(Long usuarioId, Long competicionId) {
        return urcRepository.findByUsuarioIdAndCompeticionId(usuarioId, competicionId);
    }

    // =========================================================================
    // Revocación de roles (usada desde CompeticionService)
    // =========================================================================

    /**
     * Elimina todos los roles de un usuario en una competición (expulsión completa).
     *
     * @param usuarioId    identificador del usuario al que se revocan los roles
     * @param competicionId identificador de la competición
     */
    @Transactional
    public void revocarTodosLosRoles(Long usuarioId, Long competicionId) {
        urcRepository.deleteByUsuarioIdAndCompeticionId(usuarioId, competicionId);
    }
}
