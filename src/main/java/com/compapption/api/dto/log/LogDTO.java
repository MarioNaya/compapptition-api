package com.compapption.api.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con los datos de una entrada del log de auditoría, incluyendo usuario, competición,
 * entidad afectada, acción realizada y datos anteriores/nuevos, devuelto por LogController.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private Long competicionId;
    private String competicionNombre;
    private String entidad;
    private Long entidadId;
    private String accion;
    private String datosAnteriores;
    private String datosNuevos;
    private String ipAddress;
    private LocalDateTime fecha;
}
