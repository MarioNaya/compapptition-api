package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa un equipo deportivo dentro del sistema.
 * Mapeada a la tabla {@code equipo}, almacena el nombre, descripción, URL del escudo
 * (hospedado externamente) y la visibilidad (público/privado). Los equipos privados
 * disponen de un {@code codigoInvitacion} único que su creador comparte con el admin
 * de la competición para poder ser invitados; los públicos aparecen directamente en
 * el buscador de equipos al inscribir en una competición.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    @Builder.Default
    @Column(nullable = false)
    private boolean publico = true;

    /**
     * Código único de invitación para equipos privados. Solo se rellena cuando
     * {@code publico = false}. El admin de competición lo introduce manualmente
     * para poder enviar una invitación a un equipo que no aparece en el buscador.
     */
    @Column(name = "codigo_invitacion", length = 20, unique = true)
    private String codigoInvitacion;

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
}