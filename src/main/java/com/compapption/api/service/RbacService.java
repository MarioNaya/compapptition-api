package com.compapption.api.service;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.entity.Rol;
import com.compapption.api.repository.EquipoManagerRepository;
import com.compapption.api.repository.EquipoRepository;
import com.compapption.api.repository.JugadorRepository;
import com.compapption.api.repository.UsuarioRolCompeticionRepository;
import com.compapption.api.request.invitacion.InvitacionCreateRequest;
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
    private final EquipoRepository equipoRepository;
    private final EquipoManagerRepository equipoManagerRepository;
    private final UsuarioRolCompeticionRepository urcRepository;
    private final JugadorRepository jugadorRepository;

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
     * Comprueba si el usuario puede editar resultados o estadísticas de los partidos
     * de una competición. Lo permiten los administradores de la competición y los
     * árbitros asignados a ella; los administradores del sistema superan la
     * comprobación.
     *
     * @param competicionId identificador de la competición
     * @param auth          objeto de autenticación de Spring Security
     * @return {@code true} si el usuario es admin de competición o árbitro
     */
    public boolean isAdminOrArbitroCompeticion(Long competicionId, Authentication auth) {
        if (isAdminSistema(auth)) return true;
        Long userId = extractUserId(auth);
        if (userId == null) return false;
        return urcService.tieneAlgunRol(userId, competicionId,
                List.of(Rol.RolNombre.ADMIN_COMPETICION, Rol.RolNombre.ARBITRO));
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

    /**
     * Comprueba si el usuario puede gestionar la plantilla de un equipo (añadir/quitar
     * jugadores, asignar dorsales, etc.). Aceptan:
     * <ul>
     *   <li>administradores del sistema,</li>
     *   <li>el creador del equipo,</li>
     *   <li>cualquier manager del equipo en alguna de sus competiciones,</li>
     *   <li>cualquier admin de una competición donde el equipo esté inscrito activamente.</li>
     * </ul>
     *
     * @param equipoId identificador del equipo
     * @param auth     objeto de autenticación de Spring Security
     * @return {@code true} si el usuario tiene legitimidad para tocar la plantilla
     */
    public boolean puedeGestionarPlantilla(Long equipoId, Authentication auth) {
        if (isAdminSistema(auth)) return true;
        Long userId = extractUserId(auth);
        if (userId == null || equipoId == null) return false;
        return puedeGestionarPlantillaParaUsuario(equipoId, userId);
    }

    /**
     * Variante de {@link #puedeGestionarPlantilla(Long, Authentication)} que opera
     * directamente sobre un identificador de usuario, sin pasar por {@link Authentication}.
     * Se usa en flujos internos donde el contexto de seguridad no es accesible o donde
     * interesa preguntar por un usuario distinto del que invoca (p.ej. notificaciones).
     */
    public boolean puedeGestionarPlantillaParaUsuario(Long equipoId, Long usuarioId) {
        if (equipoId == null || usuarioId == null) return false;
        if (equipoRepository.existsByIdAndCreadorId(equipoId, usuarioId)) return true;
        if (equipoManagerRepository.existsByEquipoIdAndUsuarioId(equipoId, usuarioId)) return true;
        return urcRepository.existsAdminCompeticionForEquipo(usuarioId, equipoId);
    }

    /**
     * Comprueba si el usuario puede crear un jugador "fantasma" (sin cuenta) en
     * un equipo. Requiere los mismos permisos que {@link #puedeGestionarPlantilla}.
     * Tras eliminar la distinción {@code GESTIONADO/ESTANDAR}, ambas formas de
     * incorporación (crear fantasma o invitar) están disponibles para cualquier
     * equipo.
     *
     * @param equipoId identificador del equipo
     * @param auth     objeto de autenticación de Spring Security
     * @return {@code true} si el usuario puede gestionar la plantilla del equipo
     */
    public boolean puedeCrearJugadorEnEquipo(Long equipoId, Authentication auth) {
        return puedeGestionarPlantilla(equipoId, auth);
    }

    /**
     * Comprueba si el usuario puede borrar un jugador del sistema. Solo se
     * permite a:
     * <ul>
     *   <li>el administrador del sistema,</li>
     *   <li>el propio usuario vinculado al jugador (auto-eliminación de la
     *       cuenta).</li>
     * </ul>
     * El borrado en cascada con equipos lo bloquea {@code JugadorService}: si
     * el jugador todavía está inscrito en algún equipo, hay que darlo de baja
     * primero.
     *
     * @param jugadorId identificador del jugador
     * @param auth      objeto de autenticación de Spring Security
     * @return {@code true} si el usuario puede borrar al jugador
     */
    public boolean puedeEliminarJugador(Long jugadorId, Authentication auth) {
        if (isAdminSistema(auth)) return true;
        Long userId = extractUserId(auth);
        if (userId == null || jugadorId == null) return false;
        return jugadorRepository.findById(jugadorId)
                .map(j -> j.getUsuario() != null && userId.equals(j.getUsuario().getId()))
                .orElse(false);
    }

    /**
     * Comprueba si el usuario autenticado puede emitir la invitación descrita en
     * el request, en función del rol ofrecido y del ámbito (competición/equipo):
     * <ul>
     *   <li>{@code ADMIN_COMPETICION}, {@code MANAGER_EQUIPO} y {@code ARBITRO}: el
     *       emisor debe ser admin de la competición destino.</li>
     *   <li>{@code JUGADOR}: el emisor debe poder gestionar la plantilla del equipo
     *       destino (creador del equipo, manager o admin de alguna de sus
     *       competiciones).</li>
     * </ul>
     * Los administradores del sistema superan la comprobación.
     *
     * @param request datos de la invitación a crear
     * @param auth    objeto de autenticación de Spring Security
     * @return {@code true} si el usuario está legitimado para emitir la invitación
     */
    public boolean puedeInvitar(InvitacionCreateRequest request, Authentication auth) {
        if (request == null || request.getRolOfrecido() == null) return false;
        if (isAdminSistema(auth)) return true;
        return switch (request.getRolOfrecido()) {
            case "ADMIN_COMPETICION", "MANAGER_EQUIPO", "ARBITRO" ->
                    isAdminCompeticion(request.getCompeticionId(), auth);
            case "JUGADOR" ->
                    puedeGestionarPlantilla(request.getEquipoId(), auth);
            default -> false;
        };
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails u)) return null;
        return u.getId();
    }
}
