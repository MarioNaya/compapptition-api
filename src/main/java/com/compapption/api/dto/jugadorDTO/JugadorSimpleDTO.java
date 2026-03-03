package com.compapption.api.dto.jugadorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos básicos de un jugador (nombre, apellidos, dorsal, posición y foto),
 * utilizado en listados de jugadores y como referencia embebida en EquipoDetalleDTO.
 *
 * @author Mario
 */
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
    private Byte[] foto;
}
