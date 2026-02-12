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

    private long id;
    private long eventoId;
    private long jugadorId;
    private String jugadorNombre;
    private long tipoEstadisticaId;
    private String tipoEstadisticaNombre;
    private BigDecimal valor;

}
