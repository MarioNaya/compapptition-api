package com.compapption.api.dto.eventoDTO;

import com.compapption.api.entity.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con el resultado de un evento, devolviendo el marcador local/visitante y el estado
 * del partido tras registrar o consultar el resultado vía el endpoint de resultado.
 *
 * @author Mario
 */
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
