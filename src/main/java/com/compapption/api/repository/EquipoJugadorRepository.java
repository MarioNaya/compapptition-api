package com.compapption.api.repository;

import com.compapption.api.entity.EquipoJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad EquipoJugador.
 * Gestiona la relación entre equipos y jugadores, con soporte para consultas
 * por equipo, jugador, dorsal y estado activo.
 *
 * @author Mario
 */
@Repository
public interface EquipoJugadorRepository extends JpaRepository<EquipoJugador, Long> {

    /**
     * Busca la relación equipo-jugador por sus identificadores.
     *
     * @param equipoId  identificador del equipo
     * @param jugadorId identificador del jugador
     * @return Optional con la relación, vacío si no existe
     */
    Optional<EquipoJugador> findByEquipoIdAndJugadorId(Long equipoId, Long jugadorId);

    /**
     * Obtiene los jugadores activos de un equipo cargando el jugador en la misma consulta.
     *
     * @param jugadorId identificador del equipo (parámetro mapeado a equipoId en la query)
     * @return lista de relaciones activas con el jugador cargado
     */
    @Query("SELECT ej FROM EquipoJugador ej " +
            "LEFT JOIN FETCH ej.jugador " +
            "WHERE ej.equipo.id = :equipoId AND ej.activo = true")
    List<EquipoJugador> findActivosByEquipoId(
            @Param("equipoId") long jugadorId
    );

    /**
     * Obtiene los equipos activos a los que pertenece un jugador, cargando el equipo
     * en la misma consulta.
     *
     * @param jugadorId identificador del jugador
     * @return lista de relaciones activas con el equipo cargado
     */
    @Query("SELECT ej FROM EquipoJugador ej " +
            "LEFT JOIN FETCH ej.equipo " +
            "WHERE ej.jugador.id = :jugadorId AND ej.activo = true")
    List<EquipoJugador> findActivosByJugadorId(
            @Param("jugadorId") long jugadorId
    );

    /**
     * Comprueba si un jugador está activo en un equipo.
     *
     * @param equipoId  identificador del equipo
     * @param jugadorId identificador del jugador
     * @return {@code true} si la relación existe y está activa
     */
    boolean existsByEquipoIdAndJugadorIdAndActivoTrue(long equipoId, long jugadorId);

    /**
     * Cuenta el número de jugadores activos en un equipo.
     *
     * @param equipoId identificador del equipo
     * @return número de jugadores activos en el equipo
     */
    @Query("SELECT COUNT(ej) FROM EquipoJugador ej " +
            "WHERE ej.equipo.id = :equipoId AND ej.activo = true")
    long countActivosByEquipoId(
            @Param("equipoId") long equipoId
    );

    /**
     * Busca la relación activa de un equipo para un dorsal concreto.
     * Útil para verificar si un dorsal ya está ocupado antes de asignarlo.
     *
     * @param equipoId identificador del equipo
     * @param dorsal   número de dorsal a buscar
     * @return Optional con la relación activa para ese dorsal, vacío si está libre
     */
    @Query("SELECT ej FROM EquipoJugador ej " +
            "WHERE ej.equipo.id = :equipoId " +
            "AND ej.dorsalEquipo = :dorsal " +
            "AND ej.activo = true")
    Optional<EquipoJugador> findByEquipoIdAndDorsalEquipo(
            @Param("equipoId") long equipoId,
            @Param("dorsal") int dorsal
    );
}
