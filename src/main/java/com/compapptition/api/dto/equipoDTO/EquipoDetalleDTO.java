package com.compapptition.api.dto.equipoDTO;

import com.compapptition.api.dto.jugadorDTO.JugadorSimpleDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con los datos completos de un equipo, incluyendo visibilidad (público/privado),
 * código de invitación si es privado, fecha de creación y la lista de jugadores
 * inscritos. Devuelto en el endpoint de detalle y en creación/edición de equipo.
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
    private boolean publico;
    private String codigoInvitacion;
    private LocalDateTime fechaCreacion;
    private Integer numJugadores;
    private List<JugadorSimpleDTO> jugadores;
    private Long creadorId;
    private String creadorUsername;

}
