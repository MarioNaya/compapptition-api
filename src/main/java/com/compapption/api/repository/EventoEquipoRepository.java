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
            @Param("eventoId") Long eventoId
    );

    Optional<EventoEquipo> findByEventoIdAndEquipoId(
            Long eventoId,
            Long equipoId
    );

    @Query("SELECT ee FROM EventoEquipo ee " +
            "WHERE ee.evento.id = :eventoId " +
            "AND ee.esLocal = true")
    Optional<EventoEquipo> findLocalByEventoId(
            @Param("eventoId") Long eventoId
    );

    @Query("SELECT ee FROM EventoEquipo ee " +
            "WHERE ee.evento.id = :eventoId " +
            "AND ee.esLocal = false")
    Optional<EventoEquipo> findVisitanteByEventoId(
            @Param("eventoId") Long eventoId
    );

    void deleteByEventoId(Long eventoId);
}
