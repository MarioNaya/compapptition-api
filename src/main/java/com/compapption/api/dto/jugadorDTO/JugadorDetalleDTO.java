package com.compapption.api.dto.jugadorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con los datos completos de un jugador, incluyendo el usuario vinculado y la fecha
 * de creación, devuelto en el endpoint de detalle y creación/edición de jugador.
 *
 * @author Mario
 */
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
    private String fotoUrl;
    private Long usuarioId;
    private String usuarioUsername;
    private LocalDateTime fechaCreacion;
}
