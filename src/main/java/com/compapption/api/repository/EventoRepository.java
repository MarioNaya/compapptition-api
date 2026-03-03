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

/**
 * Repositorio JPA para la entidad Evento.
 * Gestiona la búsqueda y consulta de eventos (partidos) por competición, equipo,
 * jornada, rango de fechas y estado, así como la navegación del árbol de playoff.
 *
 * @author Mario
 */
@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * Obtiene todos los eventos de una competición ordenados por jornada y fechaHora.
     *
     * @param competicionId identificador de la competición
     * @return lista de eventos ordenados cronológicamente por jornada
     */
    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "ORDER BY e.jornada, e.fechaHora")
    List<Evento> findByCompeticionIdOrdered(
            @Param("competicionId") long competicionId
    );

    /**
     * Obtiene los eventos de una competición de forma paginada.
     *
     * @param competicionId identificador de la competición
     * @param pageable      parámetros de paginación y ordenación
     * @return página de eventos de la competición
     */
    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId")
    Page<Evento> findByCompeticionId(
            @Param("competicionId") long competicionId,
            Pageable pageable
    );

    /**
     * Obtiene los eventos de una jornada concreta dentro de una competición.
     *
     * @param competicionId identificador de la competición
     * @param jornada       número de jornada
     * @return lista de eventos de esa jornada en la competición
     */
    @Query("SELECT e FROM  Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.jornada = :jornada")
    List<Evento> findByCompeticionIdAndJornada(
            @Param("competicionId") long competicionId,
            @Param("jornada") Integer jornada
    );

    /**
     * Busca un evento por su identificador cargando en la misma consulta
     * los equipos participantes (evita N+1).
     *
     * @param id identificador del evento
     * @return Optional con el evento y sus equipos, vacío si no existe
     */
    @Query("SELECT e FROM Evento e " +
            "LEFT JOIN FETCH e.equipos ee " +
            "LEFT JOIN FETCH ee.equipo " +
            "WHERE e.id = :id")
    Optional<Evento> findByIdWithEquipos(
            @Param("id") long id
    );

    /**
     * Busca un evento por su identificador cargando en la misma consulta
     * las estadísticas de jugador con su tipo (evita N+1).
     *
     * @param id identificador del evento
     * @return Optional con el evento y sus estadísticas, vacío si no existe
     */
    @Query("SELECT e FROM Evento e " +
            "LEFT JOIN FETCH e.estadisticas es " +
            "LEFT JOIN FETCH es.jugador " +
            "LEFT JOIN FETCH es.tipoEstadistica " +
            "WHERE e.id = :id")
    Optional<Evento> findByIdWithEstadisticas(
            @Param("id") long id
    );

    /**
     * Obtiene todos los eventos en los que ha participado un equipo,
     * ordenados por fecha descendente.
     *
     * @param equipoId identificador del equipo
     * @return lista de eventos del equipo ordenados por fecha descendente
     */
    @Query("SELECT e FROM Evento e " +
            "JOIN e.equipos ee " +
            "WHERE ee.equipo.id = :equipoId " +
            "ORDER BY e.fechaHora DESC")
    List<Evento> findByEquipoId(
            @Param("equipoId") long equipoId
    );

    /**
     * Obtiene los eventos de un equipo dentro de una competición,
     * ordenados por jornada y fechaHora.
     *
     * @param competicionid identificador de la competición
     * @param equipoid      identificador del equipo
     * @return lista de eventos del equipo en esa competición
     */
    @Query("SELECT e FROM Evento e " +
            "JOIN e.equipos ee " +
            "WHERE e.competicion.id = :competicionId " +
            "AND ee.equipo.id = :equipoId " +
            "ORDER BY e.jornada, e.fechaHora")
    List<Evento> findByCompeticionIdAndEquipoId(
            @Param("competicionId") Long competicionid,
            @Param("equipoId") Long equipoid);

    /**
     * Obtiene los eventos cuya fechaHora está comprendida entre dos instantes.
     *
     * @param inicio fecha y hora de inicio del rango (inclusive)
     * @param fin    fecha y hora de fin del rango (inclusive)
     * @return lista de eventos en ese rango de fechas
     */
    @Query("SELECT e FROM Evento e " +
            "WHERE e.fechaHora " +
            "BETWEEN :inicio AND :fin")
    List<Evento> findByFechaHoraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
            );

    /**
     * Obtiene los eventos de una competición cuya fechaHora está comprendida
     * entre dos instantes.
     *
     * @param competicionId identificador de la competición
     * @param inicio        fecha y hora de inicio del rango (inclusive)
     * @param fin           fecha y hora de fin del rango (inclusive)
     * @return lista de eventos de la competición en ese rango de fechas
     */
    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.fechaHora " +
            "BETWEEN :inicio AND :fin")
    List<Evento> findByCompeticionIdAndFechaHoraBetween(
            @Param("competicionId") long competicionId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    /**
     * Obtiene el número máximo de jornada registrado en una competición.
     *
     * @param competicionId identificador de la competición
     * @return número de la última jornada, o {@code null} si no hay eventos
     */
    @Query("SELECT MAX(e.jornada) FROM Evento e " +
            "WHERE e.competicion.id = :competicionId")
    Integer findMaxJornadaByCompeticionId(
            @Param("competicionId") long competicionId
    );

    /**
     * Obtiene los eventos finalizados de una competición.
     *
     * @param competicionId identificador de la competición
     * @return lista de eventos con estado FINALIZADO en la competición
     */
    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.estado = 'FINALIZADO'")
    List<Evento> findFinalizadosByCompeticionId(
            @Param("competicionId") long competicionId
    );

    /**
     * Obtiene los eventos finalizados de una competición en una temporada concreta.
     *
     * @param competicionId identificador de la competición
     * @param temporada     número de temporada
     * @return lista de eventos finalizados de esa temporada
     */
    @Query("SELECT e FROM Evento e " +
            "WHERE e.competicion.id = :competicionId " +
            "AND e.estado = 'FINALIZADO' " +
            "AND e.temporada = :temporada")
    List<Evento> findFinalizadosByCompeticionIdAndTemporada(
            @Param("competicionId") Long competicionId,
            @Param("temporada") Integer temporada);

    /**
     * Encuentra eventos de siguiente ronda que referencian el evento dado como local o visitante anterior.
     * Hace JOIN FETCH de los anteriores para evitar lazy-init al acceder a sus IDs.
     * Usado para avance automático del bracket.
     *
     * @param eventoId identificador del evento anterior
     * @return lista de eventos de la siguiente ronda que dependen del evento dado
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
     *
     * @param anteriorLocalId      identificador del evento anterior del equipo local
     * @param anteriorVisitanteId  identificador del evento anterior del equipo visitante
     * @return lista de partidos de la serie ordenados por número de partido
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
     * mismos dos equipos participantes, misma competición, numeroPartido no nulo.
     *
     * @param competicionId identificador de la competición
     * @param equipo1Id     identificador del primer equipo
     * @param equipo2Id     identificador del segundo equipo
     * @return lista de partidos de la serie de ronda 1 ordenados por número de partido
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
