package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Solicitud de vinculación entre un {@link Jugador} (sin cuenta) y un {@link Usuario}
 * registrado, sujeta a doble validación: la inicia o bien un administrador/manager
 * que tiene legitimidad sobre el equipo (estado inicial {@code PENDIENTE_USUARIO},
 * pendiente de que el usuario destino acepte), o bien el propio usuario que reclama
 * el perfil de jugador (estado inicial {@code PENDIENTE_ADMIN}, pendiente de que
 * el admin/manager del equipo apruebe).
 *
 * <p>Cuando la solicitud pasa a {@code ACEPTADA} se ejecuta el efecto: se asigna
 * {@code Jugador.usuario = Usuario}. La solicitud queda atada a un {@link Equipo}
 * concreto porque la autorización del lado admin depende del equipo del jugador
 * (un mismo jugador puede pertenecer a varios equipos a la vez).</p>
 *
 * @author Mario
 */
@Entity
@Table(name = "solicitud_vinculacion_jugador", indexes = {
        @Index(name = "idx_svj_estado", columnList = "estado"),
        @Index(name = "idx_svj_usuario", columnList = "usuario_id"),
        @Index(name = "idx_svj_equipo", columnList = "equipo_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudVinculacionJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Jugador sin cuenta al que se quiere vincular un usuario. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    /** Usuario candidato a vincular como cuenta del jugador. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Usuario que inició la solicitud (admin/manager o el propio usuario candidato). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iniciador_id", nullable = false)
    private Usuario iniciador;

    /** Equipo del jugador al que se ata la solicitud para definir el aprobador admin/manager. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Estado estado = Estado.PENDIENTE_USUARIO;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    public enum Estado {
        /** El admin/manager inició la solicitud; falta que el usuario candidato acepte. */
        PENDIENTE_USUARIO,
        /** El usuario candidato inició la solicitud; falta que el admin/manager apruebe. */
        PENDIENTE_ADMIN,
        ACEPTADA,
        RECHAZADA,
        EXPIRADA
    }
}
