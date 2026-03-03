package com.compapption.api.dto.eventoDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con la vista resumida de un evento/partido, incluyendo jornada, fecha y los equipos
 * participantes, utilizado en listados de eventos de una competición.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoSimpleDTO {

    private Long id;
    private Long competicionId;
    private String competicionNombre;
    private Integer jornada;
    private Integer temporada;
    private LocalDateTime fechaHora;
    private EventoEquipoDTO equipoLocal;
    private EventoEquipoDTO equipoVisitante;
}
