package com.compapption.api.repository;

import com.compapption.api.entity.CompeticionEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad CompeticionEquipo.
 * Gestiona la relación de inscripción entre competiciones y equipos,
 * incluyendo consultas de equipos activos y conteo de participantes.
 *
 * @author Mario
 */
@Repository
public interface CompeticionEquipoRepository extends JpaRepository<CompeticionEquipo, Long> {

    /**
     * Busca la inscripción de un equipo concreto en una competición.
     *
     * @param competicionId identificador de la competición
     * @param equipoId      identificador del equipo
     * @return Optional con la inscripción, vacío si no existe
     */
    Optional<CompeticionEquipo> findByCompeticionIdAndEquipoId(long competicionId, long equipoId);

    /**
     * Obtiene los equipos activos inscritos en una competición, cargando el equipo
     * en la misma consulta.
     *
     * @param competicionId identificador de la competición
     * @return lista de inscripciones activas con el equipo cargado
     */
    @Query("SELECT ce FROM CompeticionEquipo ce " +
            "LEFT JOIN FETCH ce.equipo " +
            "WHERE ce.competicion.id = :competicionId AND ce.activo = true")
    List<CompeticionEquipo> findActivosByCompeticionId(
            @Param("competicionId") long competicionId
    );

    /**
     * Obtiene las competiciones activas en las que está inscrito un equipo,
     * cargando la competición en la misma consulta.
     *
     * @param equipoId identificador del equipo
     * @return lista de inscripciones activas con la competición cargada
     */
    @Query("SELECT ce FROM CompeticionEquipo ce " +
            "LEFT JOIN FETCH ce.competicion " +
            "WHERE ce.equipo.id = :equipoId AND ce.activo = true")
    List<CompeticionEquipo> findActivosByEquipoId(
            @Param("equipoId") long equipoId
    );

    /**
     * Comprueba si un equipo ya está inscrito en una competición (con cualquier estado).
     *
     * @param competicionId identificador de la competición
     * @param equipoId      identificador del equipo
     * @return {@code true} si existe la inscripción
     */
    boolean existsByCompeticionIdAndEquipoId(long competicionId, long equipoId);

    /**
     * Cuenta el número de equipos activos inscritos en una competición.
     *
     * @param competicionId identificador de la competición
     * @return número de equipos activos en la competición
     */
    @Query("SELECT COUNT(ce) FROM CompeticionEquipo ce " +
            "WHERE ce.competicion.id = :competicionId AND ce.activo = true")
    long countActivosByCompeticionId(
            @Param("competicionId") long competicionId
    );
}
