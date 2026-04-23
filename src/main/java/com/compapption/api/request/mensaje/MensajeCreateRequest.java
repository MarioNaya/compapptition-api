package com.compapption.api.request.mensaje;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para enviar un mensaje dentro de una conversación existente.
 * Valida que el contenido no esté en blanco y no supere los 2000 caracteres.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeCreateRequest {
    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 2000, message = "El contenido no puede superar los 2000 caracteres")
    private String contenido;
}
