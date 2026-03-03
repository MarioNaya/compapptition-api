package com.compapption.api.request.estadistica;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Petición para registrar una estadística de un jugador en un evento. Contiene el jugador, el tipo de estadística y el valor registrado.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaRequest {

    @NotNull(message = "El jugador es obligatorio")
    private Long jugadorId;

    @NotNull(message = "El tipo de estadística es obligatorio")
    private Long tipoEstadisticaId;

    @NotNull(message = "El valor es obligatorio")
    private BigDecimal valor;

}
