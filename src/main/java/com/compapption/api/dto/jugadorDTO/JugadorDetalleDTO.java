package com.compapption.api.dto.jugadorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JugadorDetalleDTO {

    private Long id;
    private String nombre;
    private String apellidos;
    private Integer dorsal;
    private String posicion;
    private Byte[] foto;
    private Long usuarioId;
    private String usuarioUsername;
    private LocalDateTime fechaCreacion;
}
