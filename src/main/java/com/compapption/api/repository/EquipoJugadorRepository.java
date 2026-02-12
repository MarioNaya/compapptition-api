package com.compapption.api.repository;

import com.compapption.api.entity.EquipoJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoJugadorRepository extends JpaRepository<EquipoJugador, Long> {

    Optional<EquipoJugador> findByEquipoIdAndJugadorId(Long equipoId, Long jugadorId);

    @Query("SELECT ej FROM EquipoJugador ej " +
            "LEFT JOIN FETCH ej.jugador " +
            "WHERE ej.equipo.id = :equipoId AND ej.activo = true")
    List<EquipoJugador> findActivosByEquipoId(
            @Param("equipoId") long jugadorId
    );

    @Query("SELECT ej FROM EquipoJugador ej " +
            "LEFT JOIN FETCH ej.equipo " +
            "WHERE ej.jugador.id = :jugadorId AND ej.activo = true")
    List<EquipoJugador> findActivosByJugadorId(
            @Param("jugadorId") long jugadorId
    );

    boolean existsByEquipoIdAndJugadorIdAndActivoTrue(long equipoId, long jugadorId);

    @Query("SELECT COUNT(ej) FROM EquipoJugador ej " +
            "WHERE ej.equipo.id = :equipoId AND ej.activo = true")
    long countActivosByEquipoId(
            @Param("equipoId") long equipoId
    );

    @Query("SELECT ej FROM EquipoJugador ej " +
            "WHERE ej.equipo.id = :equipoId " +
            "AND ej.dorsalEquipo = :dorsal " +
            "AND ej.activo = true")
    Optional<EquipoJugador> findByEquipoIdAndDorsalEquipo(
            @Param("equipoId") long equipoId,
            @Param("dorsal") int dorsal
    );
}
