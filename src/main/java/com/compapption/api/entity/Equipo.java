package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa un equipo deportivo dentro del sistema.
 * Mapeada a la tabla {@code equipo}, almacena el nombre, descripción, URL del escudo
 * (hospedado externamente) y el tipo de equipo (GESTIONADO o ESTANDAR).
 * Se relaciona con {@link EquipoJugador}, {@link CompeticionEquipo}, {@link EventoEquipo},
 * {@link EquipoManager} y {@link Clasificacion}.
 *
 * @author Mario
 */
@Entity
@Table(name = "equipo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "escudo_url", length = 512)
    private String escudoUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoEquipo tipo = TipoEquipo.ESTANDAR;

    @Builder.Default
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EquipoJugador> jugadores = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompeticionEquipo> competiciones = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventoEquipo> eventos = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EquipoManager> managers = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Clasificacion> clasificaciones = new HashSet<>();

    public enum TipoEquipo {
        GESTIONADO,
        ESTANDAR
    }
}