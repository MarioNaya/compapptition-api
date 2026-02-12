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
}
