package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id", nullable = false)
    private Competicion competicion;

    @Column
    private int jornada;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 255)
    private String lugar;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoEvento estado = EstadoEvento.PROGRAMADO;

    @Column(name = "resultado_local")
    private int resultadoLocal;

    @Column(name = "resultado_visitante")
    private String resultadoVisitante;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_evento",nullable = false)
    private LocalDateTime fechaEvento;

    @Builder.Default
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventoEquipo> equipos = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EstadisticaJugadorEvento> estadisticas = new HashSet<>();

    public enum EstadoEvento {
        PROGRAMADO,
        EN_CURSO,
        FINALIZADO,
        SUSPENDIDO,
        APLAZADO
    }

}