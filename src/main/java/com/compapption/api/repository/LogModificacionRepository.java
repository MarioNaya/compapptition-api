package com.compapption.api.repository;

import com.compapption.api.entity.LogModificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad LogModificacion.
 * Gestiona el registro de auditoría de acciones sobre entidades del sistema,
 * con soporte para consultas paginadas por competición, usuario, entidad y rango de fechas.
 *
 * @author Mario
 */
@Repository
public interface LogModificacionRepository extends JpaRepository<LogModificacion, Long> {

    /**
     * Desvincula la competición de todos los registros de log que la referencian,
     * estableciendo la FK a {@code null}. Debe llamarse antes de eliminar una competición
     * para evitar violaciones de integridad referencial.
     *
     * @param competicionId identificador de la competición a desvincular
     */
    @Modifying
    @Query("UPDATE LogModificacion l SET l.competicion = null WHERE l.competicion.id = :competicionId")
    void clearCompeticionId(@Param("competicionId") Long competicionId);

    /**
     * Obtiene los registros de log de una competición de forma paginada,
     * ordenados por fecha descendente.
     *
     * @param competicionId identificador de la competición
     * @param pageable      parámetros de paginación y ordenación
     * @return página de registros de log de la competición
     */
    @Query("SELECT l FROM LogModificacion l " +
            "WHERE  l.competicion.id = :competicionId " +
            "ORDER BY l.fecha DESC")
    Page<LogModificacion> findByCompeticionId(
            @Param("competicionId") long competicionId,
            Pageable pageable
    );

    /**
     * Obtiene los registros de log generados por un usuario de forma paginada,
     * ordenados por fecha descendente.
     *
     * @param usuarioId identificador del usuario
     * @param pageable  parámetros de paginación y ordenación
     * @return página de registros de log del usuario
     */
    @Query("SELECT l FROM LogModificacion l " +
            "WHERE l.usuario.id = :usuarioId " +
            "ORDER BY l.fecha DESC")
    Page<LogModificacion> findByUsuarioId(
            @Param("usuarioId") long usuarioId,
            Pageable pageable
    );

    /**
     * Obtiene el historial de log de una instancia concreta de una entidad,
     * ordenado por fecha descendente.
     *
     * @param entidad   nombre de la entidad auditada (p. ej. "Competicion")
     * @param entidadId identificador de la instancia de la entidad
     * @return lista de registros de log de esa entidad
     */
    @Query("SELECT l FROM LogModificacion l " +
            "WHERE l.entidad = :entidad " +
            "AND l.entidadId = :entidadId " +
            "ORDER BY l.fecha DESC")
    List<LogModificacion> findByEntidadAndEntidadId(
            @Param("entidad") String entidad,
            @Param("entidadId") long entidadId
    );

    /**
     * Obtiene los registros de log cuya fecha esté comprendida en un rango dado,
     * de forma paginada y ordenados por fecha descendente.
     *
     * @param inicio   fecha y hora de inicio del rango (inclusive)
     * @param fin      fecha y hora de fin del rango (inclusive)
     * @param pageable parámetros de paginación y ordenación
     * @return página de registros de log en ese rango de fechas
     */
    @Query("SELECT l FROM LogModificacion l " +
            "WHERE l.fecha BETWEEN :inicio AND :fin " +
            "ORDER BY l.fecha DESC")
    Page<LogModificacion> findByFechaBetween(
            @Param("inicio")LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );

    /**
     * Obtiene los registros de log de una competición de forma paginada, cargando
     * usuario y competición en la misma consulta para evitar N+1.
     *
     * @param competicionId identificador de la competición
     * @param pageable      parámetros de paginación y ordenación
     * @return página de registros de log con usuario y competición cargados
     */
    @Query(value = "SELECT l FROM LogModificacion l " +
            "JOIN FETCH l.usuario " +
            "LEFT JOIN FETCH l.competicion " +
            "WHERE l.competicion.id = :compId " +
            "ORDER BY l.fecha DESC",
    countQuery = "SELECT COUNT(l) FROM LogModificacion l WHERE l.competicion.id = :compId")
    Page<LogModificacion> findByCompeticionIdWithDetails(@Param("compId") Long competicionId, Pageable pageable);

    /**
     * Obtiene los registros de log de un usuario de forma paginada, cargando
     * usuario y competición en la misma consulta para evitar N+1.
     *
     * @param usuarioId identificador del usuario
     * @param pageable  parámetros de paginación y ordenación
     * @return página de registros de log con usuario y competición cargados
     */
    @Query(value = "SELECT l FROM LogModificacion l " +
            "JOIN FETCH l.usuario " +
            "LEFT JOIN FETCH l.competicion " +
            "WHERE l.usuario.id = :userId " +
            "ORDER BY l.fecha DESC",
            countQuery = "SELECT COUNT(l) FROM LogModificacion l WHERE l.usuario.id = :userId")
    Page<LogModificacion> findByUsuarioIdWithDetails(@Param("userId") Long usuarioId, Pageable pageable);

    /**
     * Obtiene el historial de log detallado de una instancia concreta de una entidad,
     * cargando usuario y competición en la misma consulta.
     *
     * @param entidad   nombre de la entidad auditada
     * @param entidadId identificador de la instancia de la entidad
     * @return lista de registros de log con usuario y competición cargados
     */
    @Query("SELECT l FROM LogModificacion l " +
            "JOIN FETCH l.usuario " +
            "LEFT JOIN FETCH l.competicion " +
            "WHERE l.entidad = :entidad " +
            "AND l.entidadId = :entidadId " +
            "ORDER BY l.fecha DESC")
    List<LogModificacion> findByEntidadAndEntidadIdWithDetails(@Param("entidad") String entidad,
                                                               @Param("entidadId") Long entidadId);
}
