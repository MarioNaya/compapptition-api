package com.compapption.api.dto.estadisticaDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO con las estadísticas acumuladas de un jugador en una competición, agrupando el total
 * de un tipo de estadística concreto a lo largo de todos los eventos de la temporada.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaAcumuladaDTO {
    private Long jugadorId;
    private String jugadorNombre;
    private Long tipoEstadisticaId;
    private String tipoEstadisticaNombre;
    private BigDecimal total;
}
