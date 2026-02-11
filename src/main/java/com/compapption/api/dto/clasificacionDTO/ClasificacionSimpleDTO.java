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

    private long id;
    private long competicionId;
    private long equipoId;
    private String equipoNombre;
    private int posicion;
    private int puntos;
    private int partidosJugados;
}
