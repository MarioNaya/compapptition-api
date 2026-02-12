package com.compapption.api.repository;

import com.compapption.api.entity.EventoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventoEquipoRepository extends JpaRepository<EventoEquipo, Long> {

    @Query("SELECT ee FROM EventoEquipo ee " +
            "LEFT JOIN FETCH ee.equipo " +
            "WHERE ee.evento.id = :eventoId")
    List<EventoEquipo> findByEventoId(
            @Param("eventoId") long eventoId
    );

    Optional<EventoEquipo> findByEventoIdAndEquipoId(
            long eventoId,
            long equipoId
    );

    @Query("SELECT ee FROM EventoEquipo ee " +
            "WHERE ee.evento.id = :eventoId " +
            "AND ee.esLocal = true")
    Optional<EventoEquipo> findLocalByEventoId(
            @Param("eventoId") long eventoId
    );

    @Query("SELECT ee FROM EventoEquipo ee " +
            "WHERE ee.evento.id = :eventoId " +
            "AND ee.esLocal = false")
    Optional<EventoEquipo> findVisitanteByEventoId(
            @Param("eventoId") long eventoId
    );

    void deleteByEventoId(long eventoId);
}
