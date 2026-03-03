package com.compapption.api.mapper;

import com.compapption.api.dto.log.LogDTO;
import com.compapption.api.entity.LogModificacion;
import org.springframework.stereotype.Component;

/**
 * Mapper manual para convertir entre entidades LogModificacion y su DTO.
 * Implementado como clase {@code @Component} en lugar de interfaz MapStruct porque
 * los campos nullable (usuario, competicion) podrian romper la cadena de mapeo automatico;
 * se realizan comprobaciones nulas explicitas en cada acceso a objetos relacionados.
 *
 * @author Mario
 */
@Component
public class LogMapper {

    /**
     * Convierte una entidad LogModificacion a su DTO.
     * Maneja de forma segura los campos nullable usuario y competicion
     * comprobando su nulidad antes de acceder a sus propiedades.
     * Devuelve {@code null} si la entidad de entrada es nula.
     *
     * @param log entidad de origen
     * @return DTO con los datos del registro de auditoria, o null si log es null
     */
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
