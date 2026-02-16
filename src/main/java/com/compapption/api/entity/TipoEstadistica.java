package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

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