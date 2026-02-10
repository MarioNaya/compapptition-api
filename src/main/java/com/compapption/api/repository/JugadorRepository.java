package com.compapption.api.repository;

import com.compapption.api.entity.Jugador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    Optional<Jugador> findByUsuarioId(Long usuarioId);

    @Query("SELECT j FROM Jugador j " +
            "LEFT JOIN FETCH  j.equipos ej " +
            "LEFT JOIN FETCH ej.equipo " +
            "WHERE j.id = :id")
    Optional<Jugador> findByIdWithEquipos(
            @Param("id") Long id
    );

    @Query("SELECT DISTINCT j FROM Jugador j " +
            "JOIN j.equipos ej " +
            "WHERE ej.equipo.id = :equipoId " +
            "AND ej.activo = true")
    List<Jugador> findByEquipoId(
            @Param("equipoId") Long equipoId
    );

    @Query("SELECT j FROM Jugador j " +
            "WHERE LOWER(j.nombre) " +
            "LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(j.apellidos) " +
            "LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Jugador> searchByNombreOrApellidos(
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT j FROM Jugador j " +
            "LEFT  JOIN  FETCH  j.estadisticas e " +
            "LEFT JOIN FETCH e.tipoEstadistica " +
            "WHERE j.id = :id")
    Optional<Jugador> findByIdWithEstadisticas(
            @Param("id") Long id
    );
}
