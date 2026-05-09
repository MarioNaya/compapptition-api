package com.compapptition.api.repository;

import com.compapptition.api.entity.Rol;
import com.compapptition.api.entity.UsuarioRolCompeticion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link UsuarioRolCompeticion}.
 * Gestiona la asignación de roles de usuario en competiciones concretas,
 * con consultas optimizadas mediante JOIN FETCH para la generación de JWT.
 *
 * @author Mario
 */
@Repository
public interface UsuarioRolCompeticionRepository extends JpaRepository<UsuarioRolCompeticion, Long> {

    /**
     * Carga todos los registros rol-competicion de un usuario con sus asociaciones,
     * en una sola query. Usado al generar el JWT y al hacer refresh.
     */
    @Query("SELECT urc FROM UsuarioRolCompeticion urc " +
            "LEFT JOIN FETCH urc.rol " +
            "LEFT JOIN FETCH urc.competicion " +
            "WHERE urc.usuario.id = :usuarioId")
    List<UsuarioRolCompeticion> findByUsuarioIdWithRolesAndCompeticiones(@Param("usuarioId") Long usuarioId);

    @Query("SELECT urc FROM UsuarioRolCompeticion urc " +
            "LEFT JOIN FETCH urc.rol " +
            "WHERE urc.usuario.id = :usuarioId AND urc.competicion.id = :competicionId")
    List<UsuarioRolCompeticion> findByUsuarioIdAndCompeticionId(@Param("usuarioId") Long usuarioId,
                                                                @Param("competicionId") Long competicionId);

    @Query("SELECT urc FROM UsuarioRolCompeticion urc " +
            "LEFT JOIN FETCH urc.usuario " +
            "LEFT JOIN FETCH urc.rol " +
            "WHERE urc.competicion.id = :competicionId")
    List<UsuarioRolCompeticion> findByCompeticionId(@Param("competicionId") Long competicionId);

    Optional<UsuarioRolCompeticion> findByUsuarioIdAndCompeticionIdAndRolId(Long usuarioId,
                                                                            Long competicionId,
                                                                            Long rolId);

    boolean existsByUsuarioIdAndCompeticionIdAndRolNombre(Long usuarioId, Long competicionId, Rol.RolNombre rolNombre);

    boolean existsByUsuarioIdAndCompeticionIdAndRolNombreIn(Long usuarioId, Long competicionId, Collection<Rol.RolNombre> roles);

    boolean existsByUsuarioIdAndCompeticionId(Long usuarioId, Long competicionId);

    void deleteByUsuarioIdAndCompeticionId(Long usuarioId, Long competicionId);

    void deleteByUsuarioIdAndCompeticionIdAndRolNombre(
            long usuarioId,
            long competicionId,
            Rol.RolNombre nombre
    );

    /**
     * Comprueba si un usuario es admin de alguna competición en la que un equipo esté
     * inscrito activamente. Pregunta de autorización utilizada al gestionar la
     * plantilla del equipo: cualquier admin de una de sus competiciones puede actuar.
     *
     * @param usuarioId identificador del usuario candidato a admin
     * @param equipoId  identificador del equipo
     * @return {@code true} si existe al menos una competición activa donde el equipo
     *         está inscrito y el usuario tiene rol {@code ADMIN_COMPETICION}
     */
    @Query("SELECT COUNT(urc) > 0 FROM UsuarioRolCompeticion urc " +
            "WHERE urc.usuario.id = :usuarioId " +
            "AND urc.rol.nombre = com.compapptition.api.entity.Rol.RolNombre.ADMIN_COMPETICION " +
            "AND urc.competicion.id IN (" +
            "  SELECT ce.competicion.id FROM CompeticionEquipo ce " +
            "  WHERE ce.equipo.id = :equipoId AND ce.activo = true)")
    boolean existsAdminCompeticionForEquipo(
            @Param("usuarioId") Long usuarioId,
            @Param("equipoId") Long equipoId
    );
}
