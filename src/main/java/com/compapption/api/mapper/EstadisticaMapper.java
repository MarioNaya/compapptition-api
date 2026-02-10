package com.compapption.api.mapper;

import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.entity.EstadisticaJugadorEvento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EstadisticaMapper {

        @Mapping(target = "eventoId", source = "evento.id")
        @Mapping(target = "jugadorId", source = "jugador.id")
        @Mapping(target = "jugadorNombre", expression = "java(estadistica.getJugador().getNombre() " +
                "+ \" \" + (estadistica.getJugador().getApellidos() != null ? " +
                "estadistica.getJugador().getApellidos() : \"\"))")
        @Mapping(target = "tipoEstadisticaId", source = "tipoEstadistica.id")
        @Mapping(target = "tipoEstadisticaNombre", source = "tipoEstadistica.nombre")
        EstadisticaJugadorDTO toDTO(EstadisticaJugadorEvento estadistica);

        List<EstadisticaJugadorDTO> toDTOList(List<EstadisticaJugadorEvento> estadisticas);
}
