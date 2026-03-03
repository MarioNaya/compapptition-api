package com.compapption.api.request.evento;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Petición para crear un evento o partido. Contiene fecha y hora, lugar, jornada y los identificadores de los equipos participantes.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoCreateRequest {

    private Integer jornada;

    private Integer temporada;

    @NotNull(message = "La fecha y la hora son obligatorias")
    private LocalDateTime fechaHora;

    private String lugar;

    @NotNull(message = "El equipo local es obligatorio")
    private Long equipoLocalId;

    @NotNull(message = "El equipo visitante es obligatorio")
    private Long equipoVisitanteId;

    private String observaciones;
}
