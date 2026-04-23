package com.compapption.api.request.estadistica;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para registrar manualmente una estadística de un jugador en un evento concreto
 * desde el endpoint global {@code POST /estadisticas}.
 * <p>
 * A diferencia de {@link EstadisticaRequest}, que se utiliza anidado bajo un evento, esta
 * variante incluye explícitamente el identificador del evento y acepta el valor como
 * {@link Double} para tolerar entradas decimales desde el frontend. La validación de
 * coherencia del valor con el tipo (ENTERO, DECIMAL, BOOLEANO, TIEMPO) se hace en el
 * servicio, no aquí.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaCreateRequest {

    @NotNull(message = "El evento es obligatorio")
    private Long eventoId;

    @NotNull(message = "El jugador es obligatorio")
    private Long jugadorId;

    @NotNull(message = "El tipo de estadística es obligatorio")
    private Long tipoEstadisticaId;

    @NotNull(message = "El valor es obligatorio")
    private Double valor;
}
