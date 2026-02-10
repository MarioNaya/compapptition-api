package com.compapption.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadisticaJugadorEventoRepository extends JpaRepository<EstadisticaJugadorEventoRepository, Long> {

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.jugador " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE e.evento.id = :eventoId")
    List<EstadisticaJugadorEventoRepository> findByEventoId(
            @Param("eventoId") Long eventoId
    );

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "LEFT JOIN FETCH e.evento " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEventoRepository> findByJugadorId(
            @Param("jugadorId") Long jugadorId
    );

    Optional<EstadisticaJugadorEventoRepository> findByEventoIdAndJugadorIdAndTipoEstadisticaId(
            Long eventoId,
            Long jugadorId,
            Long tipoEstadisticaId
    );

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "WHERE e.evento.id = :eventoId AND e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEventoRepository> findByEventoIdAndJugadorId(
            @Param("eventoId") Long eventoId,
            @Param("jugadorId") Long jugadorId
    );

    void deleteByEventoId(Long eventoId);

    @Query("SELECT SUM(e.valor) FROM EstadisticaJugadorEvento e " +
            "WHERE e.jugador.id = :jugadorId " +
            "AND e.tipoEstadistica.id = :tipoEstadisticaId")
    java.math.BigDecimal sumValorByJugadorIdAndTipoEstadisticaId(
            @Param("jugadorId") Long jugadorId,
            @Param("tipoEstadistica") Long tipoEstadisticaid
    );

    @Query("SELECT e FROM EstadisticaJugadorEvento e " +
            "JOIN e.evento ev " +
            "WHERE ev.competicion.id = :competicionId AND e.jugador.id = :jugadorId")
    List<EstadisticaJugadorEventoRepository> findByCompeticionIdAndJugadorId(
            @Param("competicionId") Long competicionId,
            @Param("jugadorId") Long jugadorId
    );
}
