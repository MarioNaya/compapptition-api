package com.compapption.api.mapper;

import com.compapption.api.dto.eventoDTO.EventoDetalleDTO;
import com.compapption.api.dto.eventoDTO.EventoEquipoDTO;
import com.compapption.api.dto.eventoDTO.EventoResultadoDTO;
import com.compapption.api.dto.eventoDTO.EventoSimpleDTO;
import com.compapption.api.entity.Evento;
import com.compapption.api.entity.EventoEquipo;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventoMapper {

    @Mapping(target = "id",      source = "equipo.id")
    @Mapping(target = "nombre",  source = "equipo.nombre")
    @Mapping(target = "escudo",  source = "equipo.escudo")
    @Mapping(target = "esLocal", source = "esLocal")
    EventoEquipoDTO toEquipoDTO(EventoEquipo eventoEquipo);

    @Mapping(target = "competicionId", source = "competicion.id")
    EventoResultadoDTO toResultadoDTO(Evento evento);

    @Named("extractLocal")
    default EventoEquipoDTO extractLocal(Set<EventoEquipo> equipos) {
        if (equipos == null) return null;
        return equipos.stream()
                .filter(EventoEquipo::isEsLocal)
                .findFirst()
                .map(this::toEquipoDTO)
                .orElse(null);
    }

    @Named("extractVisitante")
    default EventoEquipoDTO extractVisitante(Set<EventoEquipo> equipos) {
        if (equipos == null) return null;
        return equipos.stream()
                .filter(ee -> !ee.isEsLocal())
                .findFirst()
                .map(this::toEquipoDTO)
                .orElse(null);
    }

    @Mapping(target = "competicionId",     source = "competicion.id")
    @Mapping(target = "competicionNombre", source = "competicion.nombre")
    @Mapping(target = "equipoLocal",       source = "equipos", qualifiedByName = "extractLocal")
    @Mapping(target = "equipoVisitante",   source = "equipos", qualifiedByName = "extractVisitante")
    EventoDetalleDTO toDetalleDTO(Evento evento);

    @Mapping(target = "competicionId",     source = "competicion.id")
    @Mapping(target = "competicionNombre", source = "competicion.nombre")
    @Mapping(target = "equipoLocal",       source = "equipos", qualifiedByName = "extractLocal")
    @Mapping(target = "equipoVisitante",   source = "equipos", qualifiedByName = "extractVisitante")
    EventoSimpleDTO toSimpleDTO(Evento evento);

    List<EventoDetalleDTO> toDetalleDTOList(List<Evento> eventos);
    List<EventoSimpleDTO>  toSimpleDTOList(List<Evento> eventos);
}