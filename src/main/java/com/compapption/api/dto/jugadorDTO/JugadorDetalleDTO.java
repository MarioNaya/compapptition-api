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

    Long id;
    private String nombre;
    private String apellidos;
    private int Dorsal;
    private String posicion;
    private byte[] foto;
    private long usuarioid;
    private String usuarioUsername;
    private LocalDateTime fechaCreacion;
}
