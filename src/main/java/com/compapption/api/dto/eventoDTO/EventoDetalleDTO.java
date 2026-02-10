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

    private Long id;
    private Long competicionId;
    private String competicionnombre;
    private Integer jornada;
    private LocalDateTime fechaHora;
    private String lugar;
    private Evento.EstadoEvento estado;
    private Integer resultadoLocal;
    private Integer resultadoVisitante;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    private EventoEquipoDTO equipoLocal;
    private EventoEquipoDTO equipoVisitante;
}
