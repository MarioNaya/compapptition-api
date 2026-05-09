package com.compapptition.api.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vista detallada de un ticket. Incluye descripción completa además de los
 * metadatos del ticket y el resumen de su autor.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetalleDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private String usuarioEmail;
    private String asunto;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
