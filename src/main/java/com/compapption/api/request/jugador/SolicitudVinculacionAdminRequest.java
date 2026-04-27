package com.compapption.api.request.jugador;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para iniciar una solicitud de vinculación desde el lado admin/manager:
 * el firmante propone que el jugador {@code path:jugadorId} se vincule a la cuenta
 * del usuario indicado. La solicitud queda atada al equipo concreto del jugador
 * para definir quién es el aprobador del lado admin si en algún momento se invierte
 * la dirección.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudVinculacionAdminRequest {

    /** Usuario candidato a recibir el perfil de jugador. */
    @NotNull(message = "El usuarioId es obligatorio")
    private Long usuarioId;

    /** Equipo del jugador al que se ata la solicitud (define el aprobador admin). */
    @NotNull(message = "El equipoId es obligatorio")
    private Long equipoId;
}
