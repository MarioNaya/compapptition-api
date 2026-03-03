package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Registra el valor de una estadística concreta de un jugador en un evento específico.
 * Mapeada a la tabla {@code estadistica_jugador_evento}, almacena el valor numérico (BigDecimal)
 * de un {@link TipoEstadistica} para un {@link Jugador} en un {@link Evento} determinado.
 * Permite agregar después las estadísticas acumuladas por temporada o competición.
 *
 * @author Mario
 */
@Entity
@Table(name = "estadistica_jugador_evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaJugadorEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_estadistica_id", nullable = false)
    private TipoEstadistica tipoEstadistica;

    @Builder.Default
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal valor = BigDecimal.ZERO;
}
