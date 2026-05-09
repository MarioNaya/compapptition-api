package com.compapptition.api.dto.jugadorDTO;

import com.compapptition.api.entity.SolicitudVinculacionJugador;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de exposición para una solicitud de vinculación entre un jugador (sin cuenta)
 * y un usuario registrado. Aplana las relaciones del agregado a los identificadores
 * y nombres mostrables que el frontend necesita para listar la bandeja de pendientes
 * y la tarjeta de detalle.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudVinculacionJugadorDTO {

    private Long id;

    private Long jugadorId;
    private String jugadorNombre;
    private String jugadorApellidos;

    private Long usuarioId;
    private String usuarioUsername;
    private String usuarioEmail;

    private Long iniciadorId;
    private String iniciadorUsername;

    private Long equipoId;
    private String equipoNombre;

    private SolicitudVinculacionJugador.Estado estado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaResolucion;
}
