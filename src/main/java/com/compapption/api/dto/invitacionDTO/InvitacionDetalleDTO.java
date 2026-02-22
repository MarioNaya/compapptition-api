package com.compapption.api.dto.invitacionDTO;

import com.compapption.api.entity.Invitacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitacionDetalleDTO {
    private Long id;
    private Long emisorId;
    private String emisorUsername;
    private String destinatarioUsername;
    private Long destinatarioId;
    private Long competicionId;
    private String competicionNombre;
    private Long equipoId;
    private String equipoNombre;
    private String rolOfrecido;
    private Invitacion.EstadoInvitacion estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
}
