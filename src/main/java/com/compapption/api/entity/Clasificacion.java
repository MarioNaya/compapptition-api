package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "clasificacion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"competicion_id", "equipo_id", "temporada"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Clasificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id", nullable = false)
    private Competicion competicion;

    @Column(nullable = false)
    private Integer temporada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Builder.Default
    @Column
    private Integer posicion = 0;

    @Builder.Default
    @Column
    private Integer puntos = 0;

    @Builder.Default
    @Column (name = "partidos_jugados")
    private Integer partidosJugados = 0;

    @Builder.Default
    @Column
    private Integer victorias = 0;

    @Builder.Default
    @Column
    private Integer empates = 0;

    @Builder.Default
    @Column
    private Integer derrotas = 0;

    @Builder.Default
    @Column(name = "goles_favor")
    private Integer golesFavor = 0;

    @Builder.Default
    @Column(name = "goles_contra")
    private Integer golesContra = 0;

    @Builder.Default
    @Column(name = "diferencia_goles")
    private Integer diferenciaGoles = 0;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}