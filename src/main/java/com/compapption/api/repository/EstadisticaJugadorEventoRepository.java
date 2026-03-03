package com.compapption.api.repository;

import com.compapption.api.entity.EstadisticaJugadorEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad EstadisticaJugadorEvento.
 * Gestiona la consulta y agregación de estadísticas individuales de jugadores
 * por evento, jugador, competición, temporada y tipo de estadística.
 *
 * @author Mario
 */
@Repository
public interface EstadisticaJugadorEventoRepository extends JpaRepository<EstadisticaJugadorEvento, Long> {

    /**
     * Obtiene todas las estadísticas de un evento cargando jugador y tipo en la misma consulta.
     *
     * @param eventoId identificador del evento
     * @return lista de estadísticas del evento con jugador y tipo cargados
     */
    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.jugador " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE e.evento.id = :eventoId")
    List<EstadisticaJugadorEvento> findByEventoId(
            @Param("eventoId") long eventoId
    );

    /**
     * Obtiene todas las estadísticas de un jugador cargando evento y tipo en la misma consulta.
     *
     * @param jugadorId identificador del jugador
     * @return lista de estadísticas del jugador con evento y tipo cargados
     */
    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.evento " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEvento> findByJugadorId(
            @Param("jugadorId") long jugadorId
    );

    /**
     * Busca la estadística exacta de un jugador en un evento para un tipo de estadística concreto.
     *
     * @param eventoId          identificador del evento
     * @param jugadorId         identificador del jugador
     * @param tipoEstadisticaId identificador del tipo de estadística
     * @return Optional con la estadística, vacío si no existe
     */
    Optional<EstadisticaJugadorEvento> findByEventoIdAndJugadorIdAndTipoEstadisticaId(
            long eventoId,
            long jugadorId,
            long tipoEstadisticaId
    );

    /**
     * Obtiene todas las estadísticas de un jugador en un evento concreto.
     *
     * @param eventoId  identificador del evento
     * @param jugadorId identificador del jugador
     * @return lista de estadísticas del jugador en ese evento
     */
    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "WHERE e.evento.id = :eventoId AND e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEvento> findByEventoIdAndJugadorId(
            @Param("eventoId") long eventoId,
            @Param("jugadorId") long jugadorId
    );

    /**
     * Obtiene las estadísticas de un jugador en una temporada concreta,
     * cargando evento y tipo en la misma consulta.
     *
     * @param jugadorId identificador del jugador
     * @param temporada número de temporada
     * @return lista de estadísticas del jugador en esa temporada
     */
    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.evento " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE e.jugador.id = :jugadorId " +
            "AND e.evento.temporada = :temporada")
    List<EstadisticaJugadorEvento> findByJugadorIdAndTemporada(
            @Param("jugadorId") Long jugadorId,
            @Param("temporada") Integer temporada);

    /**
     * Elimina todas las estadísticas asociadas a un evento.
     *
     * @param eventoId identificador del evento
     */
    void deleteByEventoId(long eventoId);

    /**
     * Calcula la suma acumulada del valor de un tipo de estadística para un jugador.
     *
     * @param jugadorId         identificador del jugador
     * @param tipoEstadisticaId identificador del tipo de estadística
     * @return suma total del valor, o {@code null} si no hay registros
     */
    @Query("SELECT SUM(e.valor) FROM EstadisticaJugadorEvento e " +
            "WHERE e.jugador.id = :jugadorId " +
            "AND e.tipoEstadistica.id = :tipoEstadisticaId")
    java.math.BigDecimal sumValorByJugadorIdAndTipoEstadisticaId(
            @Param("jugadorId") long jugadorId,
            @Param("tipoEstadisticaId") long tipoEstadisticaId
    );

    /**
     * Obtiene todas las estadísticas de un jugador en los eventos de una competición.
     *
     * @param competicionId identificador de la competición
     * @param jugadorId     identificador del jugador
     * @return lista de estadísticas del jugador en esa competición
     */
    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "JOIN e.evento ev " +
            "WHERE ev.competicion.id = :competicionId AND e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEvento> findByCompeticionIdAndJugadorId(
            @Param("competicionId") long competicionId,
            @Param("jugadorId") long jugadorId
    );

    /**
     * Obtiene todas las estadísticas de un tipo concreto en los eventos de una competición,
     * cargando jugador y tipo en la misma consulta.
     *
     * @param competicionId     identificador de la competición
     * @param tipoEstadisticaId identificador del tipo de estadística
     * @return lista de estadísticas de ese tipo en la competición
     */
    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.jugador " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "JOIN e.evento ev " +
            "WHERE ev.competicion.id = :competicionId AND e.tipoEstadistica.id = :tipoEstadisticaId")
    List<EstadisticaJugadorEvento> findByCompeticionIdAndTipoEstadisticaId(
            @Param("competicionId") Long competicionId,
            @Param("tipoEstadisticaId") Long tipoEstadisticaId);
}
