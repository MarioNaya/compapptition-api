package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "configuracion_competicion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionCompeticion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id", nullable = false, unique = true)
    private Competicion competicion;

    @Builder.Default
    @Column(name = "puntos_victoria")
    private Integer puntosVictoria = 3;

    @Builder.Default
    @Column(name = "puntos_empate")
    private Integer puntosEmpate = 1;

    @Builder.Default
    @Column(name = "puntos_derrota")
    private Integer puntosDerrota = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FormatoCompeticion formato = FormatoCompeticion.LIGA;

    @Builder.Default
    @Column(name = "dias_entre_jornadas")
    private Integer diasEntreJornadas = 7;

    @Builder.Default
    @Column(name = "num_equipos_playoff")
    private Integer numEquiposPlayoff = 8;

    @Builder.Default
    @Column(name = "partidos_eliminatoria")
    private Integer partidosEliminatoria = 1;

    public enum FormatoCompeticion {
        EVENTO_UNICO,
        LIGA,
        LIGA_IDA_VUELTA,
        PLAYOFF,
        LIGA_PLAYOFF,
        GRUPOS_PLAYOFF
    }
}