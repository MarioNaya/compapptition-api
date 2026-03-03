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
