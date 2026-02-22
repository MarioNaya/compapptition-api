package com.compapption.api.mapper;

import com.compapption.api.dto.invitacionDTO.InvitacionDetalleDTO;
import com.compapption.api.dto.invitacionDTO.InvitacionSimpleDTO;
import com.compapption.api.entity.Invitacion;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvitacionMapper {

    @Mapping(target = "emisorId", source = "emisor.id")
    @Mapping(target = "emisorUsername", source = "emisor.username")
    @Mapping(target = "destinatarioId", source = "destinatario.id")
    @Mapping(target = "destinatarioUsername", source = "destinatario.username")
    @Mapping(target = "competicionId", source = "competicion.id")
    @Mapping(target = "competicionNombre", source = "competicion.nombre")
    @Mapping(target = "equipoId", source = "equipo.id")
    @Mapping(target = "equipoNombre", source = "equipo.nombre")
    InvitacionDetalleDTO toDetalleDTO(Invitacion invitacion);

    @Mapping(target = "emisorUsername", source = "emisor.username")
    @Mapping(target = "competicionNombre", source = "competicion.nombre")
    @Mapping(target = "equipoNombre", source = "equipo.nombre")
    InvitacionSimpleDTO toSimpleDTO(Invitacion invitacion);

    List<InvitacionSimpleDTO> toSimpleDTOList(List<Invitacion> invitaciones);
}
