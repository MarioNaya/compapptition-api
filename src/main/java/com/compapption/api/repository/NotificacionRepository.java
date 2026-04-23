package com.compapption.api.repository;

import com.compapption.api.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Notificacion}. Ofrece consultas paginadas por
 * destinatario, filtrado opcional por estado de lectura, conteo de no leídas y marcado
 * masivo como leídas.
 *
 * @author Mario
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Devuelve las notificaciones de un destinatario ordenadas por fecha descendente.
     *
     * @param destinatarioId identificador del usuario destinatario
     * @param pageable       configuración de paginación
     * @return página de notificaciones
     */
    Page<Notificacion> findByDestinatarioIdOrderByFechaCreacionDesc(Long destinatarioId, Pageable pageable);

    /**
     * Devuelve las notificaciones de un destinatario filtradas por estado de lectura,
     * ordenadas por fecha descendente.
     *
     * @param destinatarioId identificador del usuario destinatario
     * @param leida          {@code true} para leídas, {@code false} para pendientes
     * @param pageable       configuración de paginación
     * @return página de notificaciones
     */
    Page<Notificacion> findByDestinatarioIdAndLeidaOrderByFechaCreacionDesc(
            Long destinatarioId, boolean leida, Pageable pageable);

    /**
     * Cuenta las notificaciones no leídas de un destinatario.
     *
     * @param destinatarioId identificador del usuario destinatario
     * @return número de notificaciones pendientes
     */
    long countByDestinatarioIdAndLeidaFalse(Long destinatarioId);

    /**
     * Marca como leídas todas las notificaciones pendientes de un destinatario.
     *
     * @param destinatarioId identificador del usuario destinatario
     * @return número de filas actualizadas
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true " +
            "WHERE n.destinatario.id = :destinatarioId AND n.leida = false")
    int marcarTodasLeidas(@Param("destinatarioId") Long destinatarioId);
}
