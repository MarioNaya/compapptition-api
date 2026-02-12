package com.compapption.api.dto.eventoDTO;

import com.compapption.api.entity.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoDetalleDTO {

    private long id;
    private long competicionId;
    private String competicionnombre;
    private int jornada;
    private LocalDateTime fechaHora;
    private String lugar;
    private Evento.EstadoEvento estado;
    private int resultadoLocal;
    private int resultadoVisitante;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    private EventoEquipoDTO equipoLocal;
    private EventoEquipoDTO equipoVisitante;
}
