package com.compapption.api.repository;

import com.compapption.api.entity.Equipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    @Query("SELECT e FROM Equipo e " +
            "LEFT JOIN FETCH e.jugadores ej " +
            "LEFT JOIN FETCH ej.jugador " +
            "WHERE e.id = :id")
    Optional<Equipo> findByIdWithJugadores(
            @Param("id") Long id
    );

    @Query("SELECT DISTINCT e FROM Equipo e " +
            "JOIN e.managers m " +
            "WHERE m.usuario.id = :usuarioId")
    List<Equipo> findByManagersId(
            @Param("usuarioId") Long usuarioId
    );

    @Query("SELECT DISTINCT e FROM Equipo e " +
            "JOIN e.competiciones ce " +
            "WHERE ce.competicion.id = : competicionId " +
            "AND ce.activo = true")
    List<Equipo> findByCompeticionId(
            @Param("competicionId") Long CompeticionId
    );

    @Query("SELECT e FROM Equipo e " +
            "WHERE LOWER(e.nombre)" +
            "LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Equipo> searchByNombre(
            @Param("nombre") String nombre,
            Pageable pageable
    );

    boolean existsByNombre(String nombre);
}
