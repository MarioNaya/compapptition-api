package com.compapption.api.dto.ticket;

import com.compapption.api.entity.EstadoTicket;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición de actualización de estado de un ticket. Reservada al admin de
 * sistema. Acepta cualquiera de los estados del enum {@link EstadoTicket}.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoTicketRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoTicket estado;
}
