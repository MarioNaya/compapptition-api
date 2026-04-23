package com.compapption.api.dto.equipoDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos básicos de un equipo (nombre, descripción y URL del escudo), utilizado
 * en listados de equipos y como referencia ligera en otras entidades.
 *
 * @author Mario
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipoSimpleDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String escudoUrl;
}
