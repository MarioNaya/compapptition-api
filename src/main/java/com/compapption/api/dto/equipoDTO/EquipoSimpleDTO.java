package com.compapption.api.dto.equipoDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipoSimpleDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private byte[] escudo;

}
