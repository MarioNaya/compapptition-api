package com.compapption.api.repository;

import com.compapption.api.entity.Clasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Clasificacion.
 * Gestiona la consulta y mantenimiento de la tabla de posiciones de cada competición,
 * con soporte para múltiples temporadas y eliminación por competición o equipo.
 *
 * @author Mario
 */
@Repository
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Long> {

    /**
     * Obtiene la clasificación completa de una competición ordenada por posición
     * ascendente, cargando el equipo en la misma consulta.
     *
     * @param competicionId identificador de la competición
     * @return lista de filas de clasificación ordenadas por posición
     */
    @Query("SELECT c FROM Clasificacion c " +
            "LEFT JOIN FETCH c.equipo " +
            "WHERE c.competicion.id = :competicionId " +
            "ORDER BY c.posicion ASC")
    List<Clasificacion> findByCompeticionIdOrderByPosicion(
            @Param("competicionId") long competicionId
    );

    /**
     * Busca la fila de clasificación de un equipo concreto en una competición.
     *
     * @param competicionId identificador de la competición
     * @param equipoId      identificador del equipo
     * @return Optional con la clasificación del equipo, vacío si no existe
     */
    Optional<Clasificacion> findByCompeticionIdAndEquipoId(
            long competicionId,
            long equipoId);

    /**
     * Obtiene todas las filas de clasificación de una competición sin ordenación fija.
     *
     * @param competicionId identificador de la competición
     * @return lista de filas de clasificación de la competición
     */
    @Query("SELECT c FROM Clasificacion c " +
            "WHERE c.competicion.id = :competicionId")
    List<Clasificacion> findByCompeticionId(
            @Param("competicionId") long competicionId
    );

    /**
     * Obtiene las filas de clasificación de una competición para una temporada concreta.
     *
     * @param competicionId identificador de la competición
     * @param temporada     número de temporada
     * @return lista de filas de clasificación de esa temporada
     */
    List<Clasificacion> findByCompeticionIdAndTemporada(
            Long competicionId,
            Integer temporada);

    /**
     * Busca la fila de clasificación de un equipo en una competición y temporada concretas.
     *
     * @param competicionId identificador de la competición
     * @param equipoId      identificador del equipo
     * @param temporada     número de temporada
     * @return Optional con la clasificación, vacío si no existe
     */
    Optional<Clasificacion> findByCompeticionIdAndEquipoIdAndTemporada(
            Long competicionId,
            Long equipoId,
            Integer temporada);

    /**
     * Elimina todas las filas de clasificación de una competición.
     *
     * @param competicionId identificador de la competición
     */
    void deleteByCompeticionId(long competicionId);

    /**
     * Elimina la fila de clasificación de un equipo concreto en una competición.
     *
     * @param competicionId identificador de la competición
     * @param equipoId      identificador del equipo
     */
    void deleteByCompeticionIdAndEquipoId(long competicionId, long equipoId);
}
