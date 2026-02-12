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

    private long id;
    private long competicionId;
    private long equipoId;
    private String equipoNombre;
    private String equipoEscudo;
    private int posicion;
    private int puntos;
    private int partidosJugados;
    private int victorias;
    private int empates;
    private int derrotas;
    private int golesFavor;
    private int golesContra;
    private int diferenciaGoles;
}
