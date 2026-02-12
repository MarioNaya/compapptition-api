package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clasificacion", uniqueConstraints = @UniqueConstraint(columnNames = {"competicion_id", "equipo_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Clasificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id", nullable = false)
    private Competicion competicion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Builder.Default
    @Column
    private int posicion = 0;

    @Builder.Default
    @Column
    private int puntos = 0;

    @Builder.Default
    @Column (name = "partidos_jugados")
    private int partidosJugados = 0;

    @Builder.Default
    @Column
    private int victorias = 0;

    @Builder.Default
    @Column
    private int empates = 0;

    @Builder.Default
    @Column
    private int derrotas = 0;

    @Builder.Default
    @Column(name = "goles_favor")
    private int golesFavor = 0;

    @Builder.Default
    @Column(name = "goles_contra")
    private int golesContra = 0;

    @Builder.Default
    @Column(name = "diferencia_goles")
    private int diferenciaGoles = 0;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}