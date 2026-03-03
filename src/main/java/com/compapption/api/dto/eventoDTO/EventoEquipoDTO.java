package com.compapption.api.dto.eventoDTO;

import com.compapption.api.entity.Equipo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de participación de un equipo en un evento, indicando si actúa
 * como local o visitante, utilizado como campo embebido en EventoSimpleDTO y EventoDetalleDTO.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoEquipoDTO {
    private Long id;
    private String nombre;
    private byte[] escudo;
    private Boolean esLocal;
}
