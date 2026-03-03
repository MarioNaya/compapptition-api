package com.compapption.api.dto.jugadorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con los datos de vinculación de un jugador con su usuario del sistema, devuelto
 * al consultar o establecer la relación jugador-usuario en el endpoint de vinculación.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JugadorUsuarioDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private LocalDateTime fechaCreacion;
}
