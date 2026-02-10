package com.compapption.api.dto.JugadorDTO;

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

    Long id;
    private String nombre;
    private String apellidos;
    private Integer Dorsal;
    private String posicion;
    private byte[] foto;
    private Long usuarioid;
    private String usuarioUsername;
    private LocalDateTime fechaCreacion;
}
