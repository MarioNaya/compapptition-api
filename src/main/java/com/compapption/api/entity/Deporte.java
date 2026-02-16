package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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
    private byte[] icono;

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