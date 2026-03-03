package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

/**
 * Tabla de unión que asocia un equipo con un evento (partido), indicando si actúa como local o visitante.
 * Mapeada a la tabla {@code evento_equipo} con restricción de unicidad sobre (evento_id, equipo_id),
 * permite identificar los dos contendientes de cada partido y su condición de local/visitante.
 * Se relaciona con {@link Evento} y {@link Equipo}.
 *
 * @author Mario
 */
@Entity
@Table(name = "evento_equipo", uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "equipo_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(name = "es_local", nullable = false)
    private boolean esLocal;
}