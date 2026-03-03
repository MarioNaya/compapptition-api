package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa un deporte disponible en el sistema (por ejemplo, fútbol, baloncesto).
 * Mapeada a la tabla {@code deporte}, almacena nombre único, descripción e icono como BLOB,
 * así como un flag de activación.
 * Se relaciona con {@link TipoEstadistica} para los tipos de estadística propios del deporte
 * y con {@link Competicion} para las competiciones que usan este deporte.
 *
 * @author Mario
 */
@Entity
@Table(name = "deporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(columnDefinition = "BLOB")
    private String icono;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "deporte", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TipoEstadistica> tipoEstadisticaSet = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "deporte")
    private Set<Competicion> competiciones = new HashSet<>();
}