package com.compapption.api.dto.clasificacionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos para actualizar manualmente una entrada de clasificación, permitiendo
 * ajustar puntos, victorias, empates, derrotas y diferencia de goles de un equipo.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificacionUpdateDTO {

    private Long id;
    private Long equipoId;
    private Integer temporada;
    private Integer posicion;
    private Integer puntos;
    private Integer partidosJugados;
    private Integer victorias;
    private Integer empates;
    private Integer derrotas;
    private Integer golesFavor;
    private Integer golesContra;
    private Integer diferenciaGoles;

}
