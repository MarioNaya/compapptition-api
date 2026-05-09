package com.compapptition.api.repository;

import com.compapptition.api.entity.EquipoJugador;
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
     * Comprueba si un usuario ya está inscrito como jugador en algún equipo distinto
     * del indicado dentro de una competición concreta. Se usa para impedir que un
     * mismo usuario juegue en varios equipos de la misma competición.
     *
     * @param usuarioId        identificador del usuario candidato
     * @param competicionId    identificador de la competición a vigilar
     * @param excludingEquipoId equipo a excluir de la búsqueda (el destino de la inscripción)
     * @return {@code true} si el usuario ya está activo como jugador en otro equipo de la competición
     */
    @Query("SELECT COUNT(ej) > 0 FROM EquipoJugador ej " +
            "JOIN ej.equipo e " +
            "JOIN e.competiciones ce " +
            "WHERE ej.jugador.usuario.id = :usuarioId " +
            "AND ce.competicion.id = :competicionId " +
            "AND ce.activo = true " +
            "AND ej.activo = true " +
            "AND ej.equipo.id <> :excludingEquipoId")
    boolean existeJugadorEnOtroEquipoDeCompeticion(
            @Param("usuarioId") long usuarioId,
            @Param("competicionId") long competicionId,
            @Param("excludingEquipoId") long excludingEquipoId);

    /**
     * Devuelve los nombres de las competiciones donde el usuario ya juega en otro equipo
     * y donde el equipo destino indicado también está inscrito. Permite detectar el
     * conflicto sin necesidad de conocer una competición concreta de antemano: cuando
     * la invitación al equipo no especifica competición, esta query aplica la regla
     * "un usuario por equipo en cada competición" sobre todo el conjunto de
     * competiciones del equipo.
     *
     * @param usuarioId        identificador del usuario candidato
     * @param equipoDestinoId  equipo al que se quiere inscribir
     * @return lista de nombres de competiciones donde existe conflicto, vacía si no hay
     */
    @Query("SELECT DISTINCT cDestino.nombre FROM CompeticionEquipo ceDestino " +
            "JOIN ceDestino.competicion cDestino " +
            "WHERE ceDestino.equipo.id = :equipoDestinoId " +
            "AND ceDestino.activo = true " +
            "AND EXISTS (" +
            "  SELECT 1 FROM EquipoJugador ej " +
            "  JOIN ej.equipo eOtro " +
            "  JOIN eOtro.competiciones ceOtro " +
            "  WHERE ej.jugador.usuario.id = :usuarioId " +
            "  AND ej.activo = true " +
            "  AND eOtro.id <> :equipoDestinoId " +
            "  AND ceOtro.activo = true " +
            "  AND ceOtro.competicion.id = cDestino.id" +
            ")")
    List<String> competicionesConConflictoJugador(
            @Param("usuarioId") long usuarioId,
            @Param("equipoDestinoId") long equipoDestinoId);

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
     * Comprueba si un jugador pertenece de forma activa a algún equipo.
     * Sustituye al patrón {@code jugador.getEquipos().isEmpty()} antes de
     * eliminar (que cargaba toda la colección lazy). Cierra A-11.
     *
     * @param jugadorId identificador del jugador
     * @return {@code true} si el jugador tiene al menos una relación activa
     */
    boolean existsByJugadorIdAndActivoTrue(long jugadorId);

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

    /**
     * Devuelve los identificadores de usuario de los jugadores activos
     * inscritos en cualquier equipo activo de la competición. Se usa para
     * notificar a los jugadores cuando hay novedades de la competición
     * (registro de resultado, cambio de estado, etc.).
     *
     * @param competicionId identificador de la competición
     * @return lista de {@code usuario.id} (sin duplicados); excluye los
     *         jugadores fantasma (sin cuenta)
     */
    @Query("SELECT DISTINCT ej.jugador.usuario.id FROM EquipoJugador ej " +
            "JOIN ej.equipo e " +
            "JOIN e.competiciones ce " +
            "WHERE ce.competicion.id = :competicionId " +
            "AND ce.activo = true " +
            "AND ej.activo = true " +
            "AND ej.jugador.usuario IS NOT NULL")
    List<Long> findUsuarioIdsJugadoresActivosByCompeticion(
            @Param("competicionId") long competicionId
    );
}
