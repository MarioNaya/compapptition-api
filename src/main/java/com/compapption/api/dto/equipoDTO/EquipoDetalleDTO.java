package com.compapption.api.dto.equipoDTO;

import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.entity.Equipo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con los datos completos de un equipo, incluyendo tipo, fecha de creación y la lista
 * de jugadores inscritos, devuelto en el endpoint de detalle y creación/edición de equipo.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoDetalleDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String escudoUrl;
    private Equipo.TipoEquipo tipo;
    private LocalDateTime fechaCreacion;
    private Integer numJugadores;
    private List<JugadorSimpleDTO> jugadores;

}
