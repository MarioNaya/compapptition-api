package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Representa la asignación de un usuario como manager de un equipo en el contexto de una competición concreta.
 * Mapeada a la tabla {@code equipo_manager} con restricción de unicidad sobre (equipo_id, competicion_id, usuario_id),
 * registra la fecha de asignación del rol de gestión.
 * Se relaciona con {@link Equipo}, {@link Competicion} y {@link Usuario}.
 *
 * @author Mario
 */
@Entity
@Table(name = "equipo_manager", uniqueConstraints = @UniqueConstraint(columnNames = {"equipo_id", "competicion_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id", nullable = false)
    private Competicion competicion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", updatable = false)
    private LocalDateTime fechaAsignacion;
}