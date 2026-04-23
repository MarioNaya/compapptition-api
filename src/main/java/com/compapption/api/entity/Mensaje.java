package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Representa un mensaje individual dentro de una {@link Conversacion} 1 a 1.
 * Mapeada a la tabla {@code mensaje}, almacena el autor, contenido (máximo 2000 caracteres),
 * fecha de envío y marca {@code leidoAt} para saber si el destinatario ya lo leyó.
 *
 * @author Mario
 */
@Entity
@Table(name = "mensaje", indexes = {
        @Index(name = "idx_mensaje_conversacion", columnList = "conversacion_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @CreationTimestamp
    @Column(name = "fecha_envio", updatable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "leido_at")
    private LocalDateTime leidoAt;
}
