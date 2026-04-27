package com.compapption.api.repository;

import com.compapption.api.entity.SolicitudVinculacionJugador;
import com.compapption.api.entity.SolicitudVinculacionJugador.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para {@link SolicitudVinculacionJugador}.
 * Consultas habituales: solicitudes pendientes de un usuario candidato, solicitudes
 * pendientes que requieren la aprobación admin/manager de un equipo, y verificación
 * de duplicados sobre la combinación jugador/usuario/equipo.
 *
 * @author Mario
 */
@Repository
public interface SolicitudVinculacionJugadorRepository
        extends JpaRepository<SolicitudVinculacionJugador, Long> {

    /**
     * Comprueba si ya existe una solicitud abierta (no resuelta) para la terna
     * jugador-usuario-equipo. Se usa para evitar duplicados al iniciar una nueva.
     */
    boolean existsByJugadorIdAndUsuarioIdAndEquipoIdAndEstadoIn(
            Long jugadorId, Long usuarioId, Long equipoId, List<Estado> estados);

    /**
     * Solicitudes pendientes de respuesta por parte del usuario candidato indicado.
     * Devuelve solo las que están en {@code PENDIENTE_USUARIO}.
     */
    @Query("SELECT s FROM SolicitudVinculacionJugador s " +
            "LEFT JOIN FETCH s.jugador j " +
            "LEFT JOIN FETCH s.equipo e " +
            "LEFT JOIN FETCH s.iniciador " +
            "WHERE s.usuario.id = :usuarioId AND s.estado = 'PENDIENTE_USUARIO'")
    List<SolicitudVinculacionJugador> findPendientesPorUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Solicitudes pendientes que requieren la aprobación del lado admin/manager,
     * filtradas por los identificadores de equipo donde el solicitante es admin/manager.
     */
    @Query("SELECT s FROM SolicitudVinculacionJugador s " +
            "LEFT JOIN FETCH s.jugador " +
            "LEFT JOIN FETCH s.usuario " +
            "LEFT JOIN FETCH s.equipo " +
            "WHERE s.estado = 'PENDIENTE_ADMIN' AND s.equipo.id IN :equipoIds")
    List<SolicitudVinculacionJugador> findPendientesPorEquipos(
            @Param("equipoIds") List<Long> equipoIds);

    /**
     * Marca como {@code EXPIRADA} todas las solicitudes pendientes con fecha de
     * expiración anterior a la indicada. Pensado para invocación periódica.
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SolicitudVinculacionJugador s SET s.estado = 'EXPIRADA' " +
            "WHERE s.estado IN ('PENDIENTE_USUARIO','PENDIENTE_ADMIN') " +
            "AND s.fechaExpiracion < :ahora")
    int marcarExpiradas(@Param("ahora") java.time.LocalDateTime ahora);

    Optional<SolicitudVinculacionJugador> findByIdAndEstado(Long id, Estado estado);
}
