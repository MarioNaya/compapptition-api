package com.compapption.api.dto.eventoDTO;

import com.compapption.api.entity.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoResultadoDTO {

    private Long id;
    private Long competicionId;
    private Integer resultadoLocal;
    private Integer resultadoVisitante;
    private Evento.EstadoEvento estado;
}
