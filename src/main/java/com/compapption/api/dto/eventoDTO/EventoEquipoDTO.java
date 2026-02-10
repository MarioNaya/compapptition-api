package com.compapption.api.dto.eventoDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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