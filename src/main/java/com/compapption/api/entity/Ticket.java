package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Ticket de soporte abierto por un usuario autenticado.
 *
 * <p>El sistema de tickets sustituye al canal mailto:* propuesto en la
 * versión 0.0.1 inicial: cualquier usuario abre incidencias desde el
 * dropdown de ayuda del navbar y el admin de sistema las atiende desde
 * el panel admin, manteniendo el historial dentro de la app sin
 * depender de un buzón externo.</p>
 *
 * <p>Cada cambio de estado por parte del admin emite una notificación
 * in-app al autor del ticket. La creación de un ticket dispara además
 * un email al admin de sistema (configurable por {@code app.support.admin-email}).</p>
 *
 * @author Mario
 */
@Entity
@Table(name = "ticket", indexes = {
        @Index(name = "idx_ticket_usuario", columnList = "usuario_id"),
        @Index(name = "idx_ticket_estado", columnList = "estado"),
        @Index(name = "idx_ticket_fecha_creacion", columnList = "fecha_creacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 160)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTicket estado = EstadoTicket.ABIERTO;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
