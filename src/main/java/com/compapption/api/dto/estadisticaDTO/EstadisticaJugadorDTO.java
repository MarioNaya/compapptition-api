package com.compapption.api.dto.estadisticaDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO con la estadística individual de un jugador en un evento específico, incluyendo
 * el tipo de estadística y el valor registrado para ese partido.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaJugadorDTO {

    private Long id;
    private Long eventoId;
    private Long jugadorId;
    private String jugadorNombre;
    private Long tipoEstadisticaId;
    private String tipoEstadisticaNombre;
    private BigDecimal valor;

}
