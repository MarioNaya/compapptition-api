package com.compapptition.api.request.jugador;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para que un usuario reclame ("este jugador soy yo") un perfil de
 * jugador sin cuenta. El firmante es el propio usuario candidato; queda
 * pendiente la aprobación del admin/manager del equipo del jugador.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudVinculacionAutoRequest {

    /** Equipo del jugador al que se ata la solicitud (define el aprobador admin). */
    @NotNull(message = "El equipoId es obligatorio")
    private Long equipoId;
}
