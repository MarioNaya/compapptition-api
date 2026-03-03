package com.compapption.api.repository;

import com.compapption.api.entity.EventoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad EventoEquipo.
 * Gestiona la participación de equipos en eventos, con identificación
 * del rol de local o visitante y eliminación por evento.
 *
 * @author Mario
 */
@Repository
public interface EventoEquipoRepository extends JpaRepository<EventoEquipo, Long> {

    /**
     * Obtiene los equipos participantes de un evento cargando el equipo
     * en la misma consulta.
     *
     * @param eventoId identificador del evento
     * @return lista de participaciones con el equipo cargado
     */
    @Query("SELECT ee FROM EventoEquipo ee " +
            "LEFT JOIN FETCH ee.equipo " +
            "WHERE ee.evento.id = :eventoId")
    List<EventoEquipo> findByEventoId(
            @Param("eventoId") long eventoId
    );

    /**
     * Busca la participación de un equipo concreto en un evento.
     *
     * @param eventoId identificador del evento
     * @param equipoId identificador del equipo
     * @return Optional con la participación, vacío si no existe
     */
    Optional<EventoEquipo> findByEventoIdAndEquipoId(
            long eventoId,
            long equipoId
    );

    /**
     * Obtiene la participación del equipo local en un evento.
     *
     * @param eventoId identificador del evento
     * @return Optional con la participación del equipo local, vacío si no existe
     */
    @Query("SELECT ee FROM EventoEquipo ee " +
            "WHERE ee.evento.id = :eventoId " +
            "AND ee.esLocal = true")
    Optional<EventoEquipo> findLocalByEventoId(
            @Param("eventoId") long eventoId
    );

    /**
     * Obtiene la participación del equipo visitante en un evento.
     *
     * @param eventoId identificador del evento
     * @return Optional con la participación del equipo visitante, vacío si no existe
     */
    @Query("SELECT ee FROM EventoEquipo ee " +
            "WHERE ee.evento.id = :eventoId " +
            "AND ee.esLocal = false")
    Optional<EventoEquipo> findVisitanteByEventoId(
            @Param("eventoId") long eventoId
    );

    /**
     * Elimina todas las participaciones de equipos en un evento.
     *
     * @param eventoId identificador del evento
     */
    void deleteByEventoId(long eventoId);
}
