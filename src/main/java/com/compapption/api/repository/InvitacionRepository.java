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

@Repository
public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {

    Optional<Invitacion> findByToken(String token);

    @Query("SELECT i FROM Invitacion i " +
            "WHERE i.destinatarioEmail = :email " +
            "AND i.estado = 'PENDIENTE'")
    List<Invitacion> findPendientesByEmail(
            @Param("email") String email
    );

    @Query("SELECT i FROM Invitacion i " +
            "WHERE  i.destinatario.id = :usuarioId " +
            "AND i.estado = 'PENDIENTE'")
    List<Invitacion> findPendientesByUsuarioId(
            @Param("usuarioId") long usuarioId
    );

    @Query("SELECT i FROM Invitacion i " +
            "WHERE i.emisor.id = :emisorId")
    List<Invitacion> findByEmisorId(
            @Param("emisorId") long emisorId
    );

    @Query("SELECT i FROM Invitacion i " +
            "WHERE i.competicion.id = :competicionId")
    List<Invitacion> findByCompeticionId(
            @Param("competicionId") long competicionId
    );

    @Modifying
    @Query("UPDATE Invitacion i SET i.estado = 'EXPIRADA' " +
            "WHERE i.estado = 'PENDIENTE' " +
            "AND i.fechaExpiracion < :now")
    int marcarExpiradas(
            @Param("now")LocalDateTime now
            );

    boolean existsByDestinatarioEmailAndCompeticionIdAndEstado(
            String email, long competicionId,
            Invitacion.EstadoInvitacion estado
    );
}
