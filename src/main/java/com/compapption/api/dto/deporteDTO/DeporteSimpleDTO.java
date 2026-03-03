package com.compapption.api.dto.deporteDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos básicos de un deporte (identificador, nombre y estado activo),
 * utilizado en listados de deportes disponibles en la plataforma.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeporteSimpleDTO {

    private Long id;
    private String nombre;
    private Boolean activo;
}
