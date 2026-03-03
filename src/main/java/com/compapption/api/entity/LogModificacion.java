package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro de auditoría que almacena cada modificación realizada sobre las entidades del sistema.
 * Mapeada a la tabla {@code log_modificacion} con índices por competición y fecha,
 * guarda la acción (CREAR, EDITAR, ELIMINAR), la entidad afectada, el usuario, la IP y los datos
 * anteriores/nuevos en formato JSON. Se persiste de forma asíncrona mediante {@code LogAsyncWriter}.
 * Se relaciona con {@link Usuario} y opcionalmente con {@link Competicion}.
 *
 * @author Mario
 */
@Entity
@Table(name = "log_modificacion", indexes = {
        @Index(name = "idx_log_competicion", columnList = "competicion_id"),
        @Index(name = "idx_log_fecha", columnList = "fecha")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogModificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicion_id")
    private Competicion competicion;

    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionLog accion;

    @Column(name = "datos_anteriores", columnDefinition = "JSON")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "JSON")
    private String datosNuevos;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fecha;

    public enum AccionLog {
        CREAR,
        EDITAR,
        ELIMINAR
    }
}
