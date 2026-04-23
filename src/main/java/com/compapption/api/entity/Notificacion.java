package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Representa una notificación persistente destinada a un usuario concreto.
 * Mapeada a la tabla {@code notificacion}, almacena el tipo (evento funcional), el
 * {@code payloadJson} con los datos adicionales (ids y nombres de entidades relacionadas),
 * el flag {@code leida} y la fecha de creación.
 * <p>
 * Las notificaciones se emiten además en tiempo real a los suscriptores SSE activos
 * del destinatario (ver {@code NotificacionService#subscribe}).
 * </p>
 *
 * @author Mario
 */
@Entity
@Table(name = "notificacion", indexes = {
        @Index(name = "idx_notificacion_destinatario", columnList = "destinatario_id"),
        @Index(name = "idx_notificacion_leida", columnList = "leida")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacion tipo;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Builder.Default
    @Column(nullable = false)
    private boolean leida = false;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Tipos funcionales de notificación que el sistema emite.
     */
    public enum TipoNotificacion {
        INVITACION_RECIBIDA,
        EQUIPO_ACEPTADO,
        RESULTADO_REGISTRADO,
        MENSAJE_RECIBIDO,
        COMPETICION_ACTIVADA
    }
}
