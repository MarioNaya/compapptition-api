package com.compapption.api.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición de creación de un ticket de soporte enviada por un usuario
 * autenticado desde el formulario in-app.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearTicketRequest {

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 160, message = "El asunto no puede superar 160 caracteres")
    private String asunto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 4000, message = "La descripción no puede superar 4000 caracteres")
    private String descripcion;
}
