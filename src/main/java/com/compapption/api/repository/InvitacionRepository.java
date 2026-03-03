package com.compapption.api.repository;

import com.compapption.api.entity.Invitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Invitacion.
 * Gestiona la búsqueda de invitaciones por token, destinatario, emisor y competición,
 * así como la expiración masiva de invitaciones pendientes.
 *
 * @author Mario
 */
@Repository
public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {

    /**
     * Busca una invitación por su token UUID único.
     *
     * @param token token UUID de la invitación
     * @return Optional con la invitación, vacío si no existe
     */
    Optional<Invitacion> findByToken(String token);

    /**
     * Obtiene las invitaciones pendientes dirigidas a un correo electrónico concreto.
     *
     * @param email dirección de correo electrónico del destinatario
     * @return lista de invitaciones pendientes para ese email
     */
    @Query("SELECT i FROM Invitacion i " +
            "WHERE i.destinatarioEmail = :email " +
            "AND i.estado = 'PENDIENTE'")
    List<Invitacion> findPendientesByEmail(
            @Param("email") String email
    );

    /**
     * Obtiene las invitaciones pendientes de un usuario registrado como destinatario.
     *
     * @param usuarioId identificador del usuario destinatario
     * @return lista de invitaciones pendientes para ese usuario
     */
    @Query("SELECT i FROM Invitacion i " +
            "WHERE  i.destinatario.id = :usuarioId " +
            "AND i.estado = 'PENDIENTE'")
    List<Invitacion> findPendientesByUsuarioId(
            @Param("usuarioId") long usuarioId
    );

    /**
     * Obtiene todas las invitaciones enviadas por un emisor concreto.
     *
     * @param emisorId identificador del usuario emisor
     * @return lista de invitaciones enviadas por el emisor
     */
    @Query("SELECT i FROM Invitacion i " +
            "WHERE i.emisor.id = :emisorId")
    List<Invitacion> findByEmisorId(
            @Param("emisorId") long emisorId
    );

    /**
     * Obtiene todas las invitaciones asociadas a una competición.
     *
     * @param competicionId identificador de la competición
     * @return lista de invitaciones de la competición
     */
    @Query("SELECT i FROM Invitacion i " +
            "WHERE i.competicion.id = :competicionId")
    List<Invitacion> findByCompeticionId(
            @Param("competicionId") long competicionId
    );

    /**
     * Marca como EXPIRADAS todas las invitaciones pendientes cuya fecha de expiración
     * sea anterior al instante indicado.
     *
     * @param now instante de referencia para la expiración
     * @return número de invitaciones actualizadas a estado EXPIRADA
     */
    @Modifying
    @Query("UPDATE Invitacion i SET i.estado = 'EXPIRADA' " +
            "WHERE i.estado = 'PENDIENTE' " +
            "AND i.fechaExpiracion < :now")
    int marcarExpiradas(
            @Param("now")LocalDateTime now
            );

    /**
     * Comprueba si existe una invitación pendiente (o en el estado indicado) para un
     * email en una competición concreta.
     *
     * @param email         correo electrónico del destinatario
     * @param competicionId identificador de la competición
     * @param estado        estado de la invitación a comprobar
     * @return {@code true} si ya existe una invitación en ese estado
     */
    boolean existsByDestinatarioEmailAndCompeticionIdAndEstado(
            String email, long competicionId,
            Invitacion.EstadoInvitacion estado
    );

    /**
     * Comprueba si existe una invitación pendiente (o en el estado indicado) para un
     * email en un equipo concreto.
     *
     * @param email    correo electrónico del destinatario
     * @param equipoId identificador del equipo
     * @param estado   estado de la invitación a comprobar
     * @return {@code true} si ya existe una invitación en ese estado
     */
    boolean existsByDestinatarioEmailAndEquipoIdAndEstado(
            String email, long equipoId,
            Invitacion.EstadoInvitacion estado
    );
}
