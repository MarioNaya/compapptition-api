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
public class EstadisticaJugadorDTO {

    private Long id;
    private Long eventoId;
    private Long jugadorId;
    private String jugadorNombre;
    private Long tipoEstadisticaId;
    private String tipoEstadisticaNombre;
    private BigDecimal valor;

}
