package com.compapption.api.dto.clasificacionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con la posición completa de un equipo en la clasificación, incluyendo todas las
 * métricas (victorias, empates, derrotas, goles a favor/contra y diferencia de goles).
 *
 * @author Mario
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClasificacionDetalleDTO {

    private Long id;
    private Long competicionId;
    private Long equipoId;
    private Integer temporada;
    private String equipoNombre;
    private String equipoEscudoUrl;
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
