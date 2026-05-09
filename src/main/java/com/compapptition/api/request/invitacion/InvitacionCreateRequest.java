package com.compapptition.api.request.invitacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para crear una invitación. Permite identificar al destinatario por
 * email o por username; debe proporcionarse al menos uno de los dos. También
 * incluye el ámbito (competición o equipo) y el rol ofrecido.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitacionCreateRequest {
    /**
     * Email del destinatario. Opcional si se proporciona {@link #destinatarioUsername}.
     */
    @Email(message = "El email no tiene un formato válido")
    private String destinatarioEmail;

    /**
     * Username del destinatario. Opcional si se proporciona {@link #destinatarioEmail}.
     * El servicio resuelve el username al email registrado del usuario.
     */
    private String destinatarioUsername;

    private Long competicionId;

    private Long equipoId;

    @NotBlank(message = "El rol ofrecido es obligatorio")
    private String rolOfrecido;
}
