package com.compapption.api.repository;

import com.compapption.api.entity.EquipoManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad EquipoManager.
 * Gestiona la asignación de managers a equipos dentro de competiciones concretas,
 * con soporte para búsqueda por equipo, competición y usuario.
 *
 * @author Mario
 */
@Repository
public interface EquipoManagerRepository extends JpaRepository<EquipoManager, Long> {

    /**
     * Busca la asignación de un manager concreto a un equipo en una competición.
     *
     * @param equipoId      identificador del equipo
     * @param competicionId identificador de la competición
     * @param usuarioId     identificador del usuario manager
     * @return Optional con la asignación, vacío si no existe
     */
    Optional<EquipoManager> findByEquipoIdAndCompeticionIdAndUsuarioId(
            long equipoId,
            long competicionId,
            long usuarioId
    );

    /**
     * Obtiene los managers de un equipo en una competición, cargando el usuario
     * en la misma consulta.
     *
     * @param equipoId      identificador del equipo
     * @param competicionId identificador de la competición
     * @return lista de asignaciones con el usuario cargado
     */
    @Query("SELECT em FROM EquipoManager em " +
            "LEFT JOIN FETCH em.usuario " +
            "WHERE em.equipo.id = :equipoId " +
            "AND em.competicion.id = :competicionId")
    List<EquipoManager> findByEquipoIdAndCompeticionId(
            @Param("equipoId") long equipoId,
            @Param("competicionId") long competicionId
    );

    /**
     * Obtiene todas las asignaciones de manager de un usuario, cargando el equipo
     * y la competición en la misma consulta.
     *
     * @param usuarioId identificador del usuario manager
     * @return lista de asignaciones del usuario con equipo y competición cargados
     */
    @Query("SELECT em FROM EquipoManager em " +
            "LEFT JOIN FETCH em.equipo " +
            "LEFT JOIN FETCH em.competicion " +
            "WHERE em.usuario.id = :usuarioId")
    List<EquipoManager> findByUsuarioId(
            @Param("usuarioId") long usuarioId
    );

    /**
     * Comprueba si un usuario ya es manager de un equipo en una competición.
     *
     * @param equipoId      identificador del equipo
     * @param competicionId identificador de la competición
     * @param usuarioId     identificador del usuario
     * @return {@code true} si la asignación existe
     */
    boolean existsByEquipoIdAndCompeticionIdAndUsuarioId(
            long equipoId,
            long competicionId,
            long usuarioId
    );

    /**
     * Comprueba si un usuario es manager de un equipo en alguna de sus competiciones.
     * Se usa en autorización cuando interesa cualquier competición donde el equipo esté inscrito.
     *
     * @param equipoId  identificador del equipo
     * @param usuarioId identificador del usuario candidato a manager
     * @return {@code true} si el usuario es manager del equipo en alguna competición
     */
    boolean existsByEquipoIdAndUsuarioId(long equipoId, long usuarioId);

    /**
     * Comprueba si un usuario ya es manager de algún equipo distinto del indicado
     * dentro de una competición concreta. Se usa para impedir que un mismo usuario
     * gestione varios equipos en la misma competición.
     *
     * @param usuarioId         identificador del usuario candidato a manager
     * @param competicionId     identificador de la competición a vigilar
     * @param excludingEquipoId equipo a excluir de la búsqueda (el destino de la asignación)
     * @return {@code true} si el usuario ya gestiona otro equipo en la competición
     */
    @Query("SELECT COUNT(em) > 0 FROM EquipoManager em " +
            "WHERE em.usuario.id = :usuarioId " +
            "AND em.competicion.id = :competicionId " +
            "AND em.equipo.id <> :excludingEquipoId")
    boolean existeManagerEnOtroEquipoDeCompeticion(
            @Param("usuarioId") long usuarioId,
            @Param("competicionId") long competicionId,
            @Param("excludingEquipoId") long excludingEquipoId);

    /**
     * Devuelve los nombres de las competiciones donde el usuario ya gestiona otro
     * equipo y donde el equipo destino también está inscrito. Permite aplicar la
     * regla "un manager por equipo en cada competición" cuando la invitación
     * MANAGER_EQUIPO no especifica competición.
     */
    @Query("SELECT DISTINCT cDestino.nombre FROM CompeticionEquipo ceDestino " +
            "JOIN ceDestino.competicion cDestino " +
            "WHERE ceDestino.equipo.id = :equipoDestinoId " +
            "AND ceDestino.activo = true " +
            "AND EXISTS (" +
            "  SELECT 1 FROM EquipoManager em " +
            "  WHERE em.usuario.id = :usuarioId " +
            "  AND em.equipo.id <> :equipoDestinoId " +
            "  AND em.competicion.id = cDestino.id" +
            ")")
    List<String> competicionesConConflictoManager(
            @Param("usuarioId") long usuarioId,
            @Param("equipoDestinoId") long equipoDestinoId);

    /**
     * Elimina todas las asignaciones de managers de un equipo en una competición.
     *
     * @param equipoId      identificador del equipo
     * @param competicionId identificador de la competición
     */
    void deleteByEquipoIdAndCompeticionId(
            long equipoId,
            long competicionId
    );

}
