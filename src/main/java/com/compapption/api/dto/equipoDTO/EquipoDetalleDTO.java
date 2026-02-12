package com.compapption.api.dto.equipoDTO;

import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.entity.Equipo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoDetalleDTO {

    private long id;
    private String nombre;
    private String descripcion;
    private String escudo;
    private Equipo.TipoEquipo tipo;
    private LocalDateTime fechaCreacion;
    private int numJugadores;
    private List<JugadorSimpleDTO> jugadores;

}
