package com.compapption.api.dto.clasificacionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con la posición resumida de un equipo en la tabla de clasificación, incluyendo
 * puntos y partidos jugados, utilizado en listados públicos de clasificación.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificacionSimpleDTO {

    private Long id;
    private Long competicionId;
    private Long equipoId;
    private Integer temporada;
    private String equipoNombre;
    private Integer posicion;
    private Integer puntos;
    private Integer partidosJugados;
}
