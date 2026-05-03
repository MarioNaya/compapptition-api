package com.compapption.api.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Proyección ligera de un ticket para listados (mis tickets, panel admin).
 * Sin la descripción completa para mantener la respuesta compacta.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSimpleDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private String asunto;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
