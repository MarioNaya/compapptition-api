package com.compapption.api.mapper;

import com.compapption.api.dto.invitacionDTO.InvitacionDetalleDTO;
import com.compapption.api.dto.invitacionDTO.InvitacionSimpleDTO;
import com.compapption.api.entity.Invitacion;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades Invitacion y sus DTOs.
 * Gestiona la transformacion de los campos anidados de emisor, destinatario,
 * competicion y equipo hacia sus respectivos ids y nombres.
 *
 * @author Mario
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvitacionMapper {

    /**
     * Convierte una entidad Invitacion a su DTO de detalle completo.
     * Mapea los datos de emisor, destinatario, competicion y equipo
     * a sus respectivos campos de id y nombre.
     *
     * @param invitacion entidad de origen
     * @return DTO con todos los datos de la invitacion
     */
    @Mapping(target = "emisorId", source = "emisor.id")
    @Mapping(target = "emisorUsername", source = "emisor.username")
    @Mapping(target = "destinatarioId", source = "destinatario.id")
    @Mapping(target = "destinatarioUsername", source = "destinatario.username")
    @Mapping(target = "competicionId", source = "competicion.id")
    @Mapping(target = "competicionNombre", source = "competicion.nombre")
    @Mapping(target = "equipoId", source = "equipo.id")
    @Mapping(target = "equipoNombre", source = "equipo.nombre")
    InvitacionDetalleDTO toDetalleDTO(Invitacion invitacion);

    /**
     * Convierte una entidad Invitacion a su DTO simple para listados.
     * Incluye unicamente el username del emisor y el nombre de la competicion.
     *
     * @param invitacion entidad de origen
     * @return DTO con los datos minimos de la invitacion
     */
    @Mapping(target = "emisorUsername", source = "emisor.username")
    @Mapping(target = "competicionNombre", source = "competicion.nombre")
    InvitacionSimpleDTO toSimpleDTO(Invitacion invitacion);

    /**
     * Convierte una lista de entidades Invitacion a una lista de DTOs simples.
     *
     * @param invitaciones lista de entidades de origen
     * @return lista de DTOs simples de invitacion
     */
    List<InvitacionSimpleDTO> toSimpleDTOList(List<Invitacion> invitaciones);
}
