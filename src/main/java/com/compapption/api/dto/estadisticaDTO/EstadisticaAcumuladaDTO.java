package com.compapption.api.dto.estadisticaDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
