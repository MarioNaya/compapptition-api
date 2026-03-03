package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa un partido o encuentro dentro de una competición deportiva.
 * Mapeada a la tabla {@code evento}, almacena la jornada, temporada, fecha/hora, lugar y estado
 * (PROGRAMADO, EN_CURSO, FINALIZADO, SUSPENDIDO, APLAZADO), así como referencias al bracket de playoff
 * mediante {@code partidoAnteriorLocal} y {@code partidoAnteriorVisitante}.
 * Se relaciona con {@link Competicion}, {@link EventoEquipo} y {@link EstadisticaJugadorEvento}.
 *
 * @author Mario
 */
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id", nullable = false)
    private Competicion competicion;

    @Column
    private Integer jornada;

    @Column
    private Integer temporada;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @PrePersist
    @PreUpdate
    private void sincronizarFechaEvento() {
        this.fechaEvento = this.fechaHora;
    }

    @Column(length = 255)
    private String lugar;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoEvento estado = EstadoEvento.PROGRAMADO;

    @Column(name = "resultado_local")
    private Integer resultadoLocal;

    @Column(name = "resultado_visitante")
    private Integer resultadoVisitante;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partido_anterior_local_id")
    private Evento partidoAnteriorLocal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partido_anterior_visitante_id")
    private Evento partidoAnteriorVisitante;

    @Column(name = "numero_partido")
    private Integer numeroPartido;

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