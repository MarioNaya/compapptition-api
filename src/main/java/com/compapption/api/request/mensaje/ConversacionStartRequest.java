package com.compapption.api.request.mensaje;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para iniciar (o reutilizar) una conversación 1 a 1 con otro usuario, indicando
 * el identificador del destinatario.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionStartRequest {
    @NotNull(message = "El id del destinatario es obligatorio")
    private Long destinatarioId;
}
