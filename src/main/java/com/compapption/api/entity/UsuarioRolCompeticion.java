package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Asigna un rol específico a un usuario dentro del contexto de una competición concreta (RBAC por competición).
 * Mapeada a la tabla {@code usuario_rol_competicion} con unicidad sobre (usuario_id, competicion_id, rol_id),
 * registra la fecha de asignación y permite que un usuario tenga distintos roles en distintas competiciones.
 * Se relaciona con {@link Usuario}, {@link Competicion} y {@link Rol}.
 *
 * @author Mario
 */
@Entity
@Table(name = "usuario_rol_competicion", uniqueConstraints = @UniqueConstraint(columnNames = {
        "usuario_id", "competicion_id", "rol_id"
}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRolCompeticion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id", nullable = false)
    private Competicion competicion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", updatable = false)
    private LocalDateTime fechaAsignacion;
}