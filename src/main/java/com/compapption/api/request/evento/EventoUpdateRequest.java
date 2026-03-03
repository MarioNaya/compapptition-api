package com.compapption.api.request.evento;

import com.compapption.api.entity.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Petición para actualizar un evento existente. Permite modificar la fecha, lugar, estado y observaciones del partido.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoUpdateRequest {

    private Integer jornada;
    private Integer temporada;
    private LocalDateTime fechaHora;
    private String lugar;
    private Evento.EstadoEvento estado;
    private String observaciones;
}
