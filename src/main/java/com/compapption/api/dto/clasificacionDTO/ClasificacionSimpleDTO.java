package com.compapption.api.dto.clasificacionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificacionSimpleDTO {

    private Long id;
    private Long competicionId;
    private Long equipoId;
    private String equipoNombre;
    private Integer posicion;
    private Integer puntos;
    private Integer partidosJugados;
}
