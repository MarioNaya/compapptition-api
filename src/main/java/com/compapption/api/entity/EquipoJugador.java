package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tabla de unión que representa la pertenencia de un jugador a un equipo con su dorsal asignado.
 * Mapeada a la tabla {@code equipo_jugador} con restricción de unicidad sobre (equipo_id, jugador_id),
 * almacena el dorsal específico en ese equipo, el estado activo y las fechas de alta y baja.
 * Se relaciona con {@link Equipo} y {@link Jugador}.
 *
 * @author Mario
 */
@Entity
@Table(name = "equipo_jugador", uniqueConstraints = @UniqueConstraint(columnNames = {"equipo_id","jugador_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Column(name = "dorsal_equipo")
    private Integer dorsalEquipo;

    @Builder.Default
    @Column(nullable = false)
    private  boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_alta", updatable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;
}