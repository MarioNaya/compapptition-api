package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad central del sistema que representa una competición deportiva.
 * Mapeada a la tabla {@code competicion}, agrupa el estado (BORRADOR, ACTIVA, FINALIZADA, CANCELADA),
 * el formato, las fechas y las relaciones con equipos inscritos, eventos, clasificaciones y configuración.
 * Se relaciona con {@link Deporte}, {@link ConfiguracionCompeticion}, {@link CompeticionEquipo},
 * {@link Evento}, {@link Clasificacion} y {@link UsuarioRolCompeticion}.
 *
 * @author Mario
 */
@Entity
@Table(name = "competicion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deporte_id", nullable = false)
    private Deporte deporte;

    @Builder.Default
    @Column(name = "temporada_actual")
    private Integer temporadaActual = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    @Builder.Default
    @Column(nullable = false)
    private boolean publica = true;

    @Builder.Default
    @Column(name = "inscripcion_abierta", nullable = false)
    private boolean inscripcionAbierta = true;

    @Builder.Default
    @Column(name = "estadisticas_activas", nullable = false)
    private boolean estadisticasActivas = true;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoCompeticion estado = EstadoCompeticion.BORRADOR;

    @CreationTimestamp
    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDate fechaActualizacion;

    @OneToOne(mappedBy = "competicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private ConfiguracionCompeticion configuracion;

    @Builder.Default
    @OneToMany(mappedBy = "competicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompeticionEquipo> equipos = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "competicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Evento> eventos = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "competicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Clasificacion> clasificaciones = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "competicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRolCompeticion> usuariosRol = new HashSet<>();

    public enum EstadoCompeticion{
        BORRADOR,
        ACTIVA,
        FINALIZADA,
        CANCELADA
    }
}
