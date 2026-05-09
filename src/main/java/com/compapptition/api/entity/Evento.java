package com.compapptition.api.entity;

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

    /**
     * Flag idempotente para la notificación automática del partido próximo.
     * Lo marca {@code EventoService.notificarPartido} tras enviar los emails
     * a los jugadores y evita que el {@code NotificacionPartidoScheduler}
     * vuelva a notificar el mismo evento si cae en su ventana en ticks
     * sucesivos. La notificación manual desde el admin de competición
     * lo ignora (forzar=true) para soportar cambios de fecha.
     */
    @Builder.Default
    @Column(name = "notificado_partido", nullable = false)
    private boolean notificadoPartido = false;

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