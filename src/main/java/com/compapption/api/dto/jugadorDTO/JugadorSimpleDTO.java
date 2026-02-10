package com.compapption.api.dto.jugadorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JugadorSimpleDTO {

    private Long id;
    private String nombre;
    private String apellidos;
    private Integer dorsal;
    private String posicion;
    private byte[] foto;
}
