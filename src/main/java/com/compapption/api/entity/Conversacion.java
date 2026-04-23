package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa una conversación privada 1 a 1 entre dos usuarios del sistema.
 * Mapeada a la tabla {@code conversacion}, almacena los dos participantes (usuarioA y usuarioB),
 * la fecha de creación y la fecha del último mensaje (para ordenar la bandeja de entrada).
 * <p>
 * Para evitar conversaciones duplicadas entre los mismos dos usuarios, los identificadores
 * se normalizan en el servicio (usuarioA siempre con el id menor), con restricción de
 * unicidad sobre el par (usuario_a_id, usuario_b_id).
 * </p>
 *
 * @author Mario
 */
@Entity
@Table(
        name = "conversacion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_a_id", "usuario_b_id"}),
        indexes = {
                @Index(name = "idx_conversacion_usuario_a", columnList = "usuario_a_id"),
                @Index(name = "idx_conversacion_usuario_b", columnList = "usuario_b_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_a_id", nullable = false)
    private Usuario usuarioA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_b_id", nullable = false)
    private Usuario usuarioB;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultimo_mensaje")
    private LocalDateTime fechaUltimoMensaje;

    @Builder.Default
    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Mensaje> mensajes = new HashSet<>();
}
