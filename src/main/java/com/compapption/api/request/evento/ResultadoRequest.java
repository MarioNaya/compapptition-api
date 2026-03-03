package com.compapption.api.request.evento;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para registrar el resultado de un evento. Contiene el marcador del equipo local y del equipo visitante.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoRequest {

    @NotNull(message = "El resultado local es obligatorio")
    @Min(value = 0, message = "El resultado no puede ser negativo")
    private Integer resultadoLocal;

    @NotNull(message = "El resultado visitante es obligatorio")
    @Min(value = 0, message = "El resultado no puede ser negativo")
    private Integer resultadoVisitante;
}
