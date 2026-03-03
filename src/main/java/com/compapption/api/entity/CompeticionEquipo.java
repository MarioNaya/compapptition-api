package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tabla de unión que representa la inscripción de un equipo en una competición.
 * Mapeada a la tabla {@code competicion_equipo} con restricción de unicidad sobre (competicion_id, equipo_id),
 * registra la fecha de inscripción y un flag de activación para controlar altas y bajas sin eliminar el registro.
 * Se relaciona con {@link Competicion} y {@link Equipo}.
 *
 * @author Mario
 */
@Entity
@Table(name = "competicion_equipo", uniqueConstraints = @UniqueConstraint(columnNames  = {"competicion_id", "equipo_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompeticionEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id", nullable = false)
    private Competicion competicion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @CreationTimestamp
    @Column(name = "fecha_inscripcion", updatable = false)
    private LocalDateTime fechaInscripcion;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;
}
