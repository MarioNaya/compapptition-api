package com.compapption.api.repository;

import com.compapption.api.entity.LogModificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogModificacionRepository extends JpaRepository<LogModificacion, Long> {

    @Query("SELECT l FROM LogModificacion l " +
            "WHERE  l.competicion.id = :competicionId " +
            "ORDER BY l.fecha DESC")
    Page<LogModificacion> findByCompeticionId(
            @Param("competicionId") Long competicionId,
            Pageable pageable
    );

    @Query("SELECT l FROM LogModificacion l " +
            "WHERE l.usuario.id = :usuarioId " +
            "ORDER BY l.fecha DESC")
    Page<LogModificacion> findByUsuarioId(
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );

    @Query("SELECT l FROM LogModificacion l " +
            "WHERE l.entidad = :entidad " +
            "AND l.entidadId = :entidadId " +
            "ORDER BY l.fecha DESC")
    List<LogModificacion> findByEntidadAndEntidadId(
            @Param("entidad") String entidad,
            @Param("entidadId") Long entidadId
    );

    @Query("SELECT l FROM LogModificacion l " +
            "WHERE l.fecha BETWEEN :inicio AND :fin " +
            "ORDER BY l.fecha DESC")
    Page<LogModificacion> findByFechaBetween(
            @Param("inicio")LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );
}
