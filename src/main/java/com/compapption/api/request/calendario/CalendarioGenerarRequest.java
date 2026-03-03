package com.compapption.api.request.calendario;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Petición para generar el calendario de una competición. Contiene la fecha de inicio y los días entre jornadas.
 *
 * @author Mario
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarioGenerarRequest {

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotNull(message = "Se debe especificar los días entre jornada")
    private Integer diasJornada;
}
