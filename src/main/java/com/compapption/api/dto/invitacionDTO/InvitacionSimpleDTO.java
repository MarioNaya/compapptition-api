package com.compapption.api.dto.invitacionDTO;

import com.compapption.api.entity.Invitacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con la vista resumida de una invitación, incluyendo emisor, destinatario, competición,
 * rol ofrecido y estado, utilizado en listados de invitaciones enviadas y recibidas.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitacionSimpleDTO {
    private Long id;
    private String emisorUsername;
    private String destinatarioUsername;
    private String competicionNombre;
    private String rolOfrecido;
    private Invitacion.EstadoInvitacion estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
}
