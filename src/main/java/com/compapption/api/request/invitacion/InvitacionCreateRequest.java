package com.compapption.api.request.invitacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitacionCreateRequest {
    @NotBlank(message = "El email del destinatario es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String destinatarioEmail;

    private Long competicionId;

    private Long equipoId;

    @NotBlank(message = "El rol ofrecido es obligatorio")
    private String rolOfrecido;
}
