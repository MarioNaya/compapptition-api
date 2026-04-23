package com.compapption.api.dto.mensaje;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de un mensaje individual dentro de una conversación. Expone el id del mensaje,
 * la conversación a la que pertenece, los datos del autor, el contenido, la fecha de envío
 * y el flag {@code leido} derivado de la marca {@code leidoAt} en la entidad.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeDTO {
    private Long id;
    private Long conversacionId;
    private Long autorId;
    private String autorUsername;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private boolean leido;
}
