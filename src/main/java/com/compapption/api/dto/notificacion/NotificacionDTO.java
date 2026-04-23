package com.compapption.api.dto.notificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para las notificaciones entregadas al cliente. El campo {@code payload} se deserializa
 * desde el {@code payloadJson} de la entidad hacia un mapa clave-valor genérico para que
 * el frontend pueda consumirlo sin acoplarse a un esquema rígido.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionDTO {
    private Long id;
    private String tipo;
    private Map<String, Object> payload;
    private boolean leida;
    private LocalDateTime fechaCreacion;
}
