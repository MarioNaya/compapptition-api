package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Define un tipo de estadística medible dentro de un deporte (por ejemplo, goles, asistencias, rebotes).
 * Mapeada a la tabla {@code tipo_estadistica} con unicidad sobre (deporte_id, nombre),
 * especifica el tipo de valor (ENTERO, DECIMAL, BOOLEANO, TIEMPO), el orden de presentación y si está activo.
 * Se relaciona con {@link Deporte} y es referenciada por {@link EstadisticaJugadorEvento}.
 *
 * @author Mario
 */
@Entity
@Table(name = "tipo_estadistica", uniqueConstraints = @UniqueConstraint(columnNames = {
        "deporte_id", "nombre"
}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstadistica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deporte_id", nullable = false)
    private Deporte deporte;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_valor", length = 20)
    private TipoValor tipoValor = TipoValor.ENTERO;

    @Builder.Default
    @Column
    private Integer orden = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    public enum TipoValor {
        ENTERO,
        DECIMAL,
        BOOLEANO,
        TIEMPO
    }
}