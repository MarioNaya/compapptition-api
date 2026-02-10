package com.compapption.api.dto.clasificacionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClasificacionDetalleDTO {

    private Long id;
    private Long competicionId;
    private Long equipoId;
    private String equipoNombre;
    private String equipoEscudo;
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
