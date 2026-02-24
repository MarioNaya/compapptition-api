package com.compapption.api.repository;

import com.compapption.api.entity.Equipo;
import com.compapption.api.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "ORDER BY e.jornada, e.fechaHora")
    List<Evento> findByCompeticionIdOrdered(
            @Param("competicionId") long competicionId
    );

    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId")
    Page<Evento> findByCompeticionId(
            @Param("competicionId") long competicionId,
            Pageable pageable
    );

    @Query("SELECT e FROM  Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.jornada = :jornada")
    List<Evento> findByCompeticionIdAndJornada(
            @Param("competicionId") long competicionId,
            @Param("jornada") Integer jornada
    );

    @Query("SELECT e FROM Evento e " +
            "LEFT JOIN FETCH e.equipos ee " +
            "LEFT JOIN FETCH ee.equipo " +
            "WHERE e.id = :id")
    Optional<Evento> findByIdWithEquipos(
            @Param("id") long id
    );

    @Query("SELECT e FROM Evento e " +
            "LEFT JOIN FETCH e.estadisticas es " +
            "LEFT JOIN FETCH es.jugador " +
            "LEFT JOIN FETCH es.tipoEstadistica " +
            "WHERE e.id = :id")
    Optional<Evento> findByIdWithEstadisticas(
            @Param("id") long id
    );

    @Query("SELECT e FROM Evento e " +
            "JOIN e.equipos ee " +
            "WHERE ee.equipo.id = :equipoId " +
            "ORDER BY e.fechaHora DESC")
    List<Evento> findByEquipoId(
            @Param("equipoId") long equipoId
    );

    @Query("SELECT e FROM Evento e " +
            "JOIN e.equipos ee " +
            "WHERE e.competicion.id = :competicionId " +
            "AND ee.equipo.id = :equipoId " +
            "ORDER BY e.jornada, e.fechaHora")
    List<Evento> findByCompeticionIdAndEquipoId(
            @Param("competicionId") Long competicionid,
            @Param("equipoId") Long equipoid);

    @Query("SELECT e FROM Evento e " +
            "WHERE e.fechaHora " +
            "BETWEEN :inicio AND :fin")
    List<Evento> findByFechaHoraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
            );

    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.fechaHora " +
            "BETWEEN :inicio AND :fin")
    List<Evento> findByCompeticionIdAndFechaHoraBetween(
            @Param("competicionId") long competicionId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT MAX(e.jornada) FROM Evento e " +
            "WHERE e.competicion.id = :competicionId")
    Integer findMaxJornadaByCompeticionId(
            @Param("competicionId") long competicionId
    );

    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.estado = 'FINALIZADO'")
    List<Evento> findFinalizadosByCompeticionId(
            @Param("competicionId") long competicionId
    );

    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.estado = 'FINALIZADO' " +
            "AND e.temporada = :temporada")
    List<Evento> findFinalizadosByCompeticionIdAndTemporada(
            @Param("competicionId") Long competicionId,
            @Param("temporada") Integer temporada);

    /**
     * Encuentra eventos de siguiente ronda que referencian el evento dado como local o visitante anterior.
     * Hace JOIN FETCH de los anteriors para evitar lazy-init al acceder a sus IDs.
     * Usado para avance automático del bracket.
     */
    @Query("SELECT e FROM Evento e " +
            "LEFT JOIN FETCH e.partidoAnteriorLocal " +
            "LEFT JOIN FETCH e.partidoAnteriorVisitante " +
            "WHERE e.partidoAnteriorLocal.id = :eventoId " +
            "OR e.partidoAnteriorVisitante.id = :eventoId")
    List<Evento> findByPartidoAnteriorId(@Param("eventoId") Long eventoId);

    /**
     * Encuentra todos los partidos de una serie de rondas 2+:
     * comparten los mismos eventos anteriores (mismo anteriorLocal y anteriorVisitante).
     */
    @Query("SELECT e FROM Evento e " +
            "WHERE e.partidoAnteriorLocal.id = :anteriorLocalId " +
            "AND e.partidoAnteriorVisitante.id = :anteriorVisitanteId " +
            "ORDER BY e.numeroPartido")
    List<Evento> findSerieByAnteriores(
            @Param("anteriorLocalId") Long anteriorLocalId,
            @Param("anteriorVisitanteId") Long anteriorVisitanteId);

    /**
     * Encuentra todos los partidos de una serie de ronda 1:
     * mismos dos equipos participantes, mismo competicion, numeroPartido no nulo.
     */
    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.partidoAnteriorLocal IS NULL " +
            "AND e.numeroPartido IS NOT NULL " +
            "AND EXISTS (SELECT ee FROM EventoEquipo ee WHERE ee.evento = e AND ee.equipo.id = :equipo1Id) " +
            "AND EXISTS (SELECT ee FROM EventoEquipo ee WHERE ee.evento = e AND ee.equipo.id = :equipo2Id) " +
            "ORDER BY e.numeroPartido")
    List<Evento> findSerieRonda1ByEquipos(
            @Param("competicionId") Long competicionId,
            @Param("equipo1Id") Long equipo1Id,
            @Param("equipo2Id") Long equipo2Id);
}
