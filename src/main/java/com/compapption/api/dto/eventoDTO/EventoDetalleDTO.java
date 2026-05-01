package com.compapption.api.dto.eventoDTO;

import com.compapption.api.entity.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con la vista completa de un evento/partido, incluyendo lugar, estado, resultado,
 * observaciones, equipos participantes y datos del bracket de playoff.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoDetalleDTO {

    private Long id;
    private Long competicionId;
    private String competicionNombre;
    private Integer jornada;
    private Integer temporada;
    private LocalDateTime fechaHora;
    private String lugar;
    private Evento.EstadoEvento estado;
    private Integer resultadoLocal;
    private Integer resultadoVisitante;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    private EventoEquipoDTO equipoLocal;
    private EventoEquipoDTO equipoVisitante;

    private Long partidoAnteriorLocalId;
    private Long partidoAnteriorVisitanteId;
    private Integer numeroPartido;

    /**
     * Indica si el partido es de playoff y depende de una fase aún no terminada
     * (liga, grupos o ronda anterior del bracket). Cuando es {@code true} el
     * frontend debe deshabilitar la edición y el backend rechaza mutaciones
     * sobre el evento.
     */
    private boolean bloqueado;
}
