package com.compapption.api.repository;

import com.compapption.api.entity.EstadisticaJugadorEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadisticaJugadorEventoRepository extends JpaRepository<EstadisticaJugadorEvento, Long> {

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.jugador " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE e.evento.id = :eventoId")
    List<EstadisticaJugadorEvento> findByEventoId(
            @Param("eventoId") long eventoId
    );

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.evento " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEvento> findByJugadorId(
            @Param("jugadorId") long jugadorId
    );

    Optional<EstadisticaJugadorEvento> findByEventoIdAndJugadorIdAndTipoEstadisticaId(
            long eventoId,
            long jugadorId,
            long tipoEstadisticaId
    );

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "WHERE e.evento.id = :eventoId AND e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEvento> findByEventoIdAndJugadorId(
            @Param("eventoId") long eventoId,
            @Param("jugadorId") long jugadorId
    );

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.evento " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE e.jugador.id = :jugadorId " +
            "AND e.evento.temporada = :temporada")
    List<EstadisticaJugadorEvento> findByJugadorIdAndTemporada(
            @Param("jugadorId") Long jugadorId,
            @Param("temporada") Integer temporada);

    void deleteByEventoId(long eventoId);

    @Query("SELECT SUM(e.valor) FROM EstadisticaJugadorEvento e " +
            "WHERE e.jugador.id = :jugadorId " +
            "AND e.tipoEstadistica.id = :tipoEstadisticaId")
    java.math.BigDecimal sumValorByJugadorIdAndTipoEstadisticaId(
            @Param("jugadorId") long jugadorId,
            @Param("tipoEstadisticaId") long tipoEstadisticaId
    );

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "JOIN e.evento ev " +
            "WHERE ev.competicion.id = :competicionId AND e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEvento> findByCompeticionIdAndJugadorId(
            @Param("competicionId") long competicionId,
            @Param("jugadorId") long jugadorId
    );

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.jugador " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "JOIN e.evento ev " +
            "WHERE ev.competicion.id = :competicionId AND e.tipoEstadistica.id = :tipoEstadisticaId")
    List<EstadisticaJugadorEvento> findByCompeticionIdAndTipoEstadisticaId(
            @Param("competicionId") Long competicionId,
            @Param("tipoEstadisticaId") Long tipoEstadisticaId);
}
