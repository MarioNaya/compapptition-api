package com.compapption.api.mapper;

import com.compapption.api.dto.log.LogDTO;
import com.compapption.api.entity.LogModificacion;
import org.springframework.stereotype.Component;

/**
 * No hacemos interfaz en el caso del LogMapper porque los campos null como usuario pueden romper la cadena
 * de mapeo porque algunos atributos se adquieren a través de objetos nullables
 */
@Component
public class LogMapper {

    public LogDTO toDTO(LogModificacion log) {
        if (log == null) return null;

        return LogDTO.builder()
                .id(log.getId())
                .usuarioId(log.getUsuario() != null ? log.getUsuario().getId() : null)
                .usuarioUsername(log.getUsuario() != null ? log.getUsuario().getUsername() : null)
                .competicionId(log.getCompeticion() != null ? log.getCompeticion().getId() : null)
                .competicionNombre(log.getCompeticion() != null ? log.getCompeticion().getNombre() : null)
                .entidad(log.getEntidad())
                .entidadId(log.getEntidadId())
                .accion(log.getAccion() != null ? log.getAccion().name() : null)
                .datosAnteriores(log.getDatosAnteriores())
                .datosNuevos(log.getDatosNuevos())
                .ipAddress(log.getIpAddress())
                .fecha(log.getFecha())
                .build();
    }
}
