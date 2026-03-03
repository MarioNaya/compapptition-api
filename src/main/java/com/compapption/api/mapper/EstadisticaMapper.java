package com.compapption.api.mapper;

import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.entity.EstadisticaJugadorEvento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades EstadisticaJugadorEvento y sus DTOs.
 * Gestiona la construccion del nombre completo del jugador mediante expresion Java
 * concatenando nombre y apellidos (con soporte a apellidos nulos).
 *
 * @author Mario
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EstadisticaMapper {

        /**
         * Convierte una entidad EstadisticaJugadorEvento a su DTO de estadistica.
         * Mapea los ids de evento, jugador y tipo de estadistica, y construye el nombre
         * completo del jugador concatenando nombre y apellidos mediante expresion Java.
         *
         * @param estadistica entidad de origen
         * @return DTO con los datos de la estadistica del jugador en el evento
         */
        @Mapping(target = "eventoId", source = "evento.id")
        @Mapping(target = "jugadorId", source = "jugador.id")
        @Mapping(target = "jugadorNombre", expression = "java(estadistica.getJugador().getNombre() " +
                "+ \" \" + (estadistica.getJugador().getApellidos() != null ? " +
                "estadistica.getJugador().getApellidos() : \"\"))")
        @Mapping(target = "tipoEstadisticaId", source = "tipoEstadistica.id")
        @Mapping(target = "tipoEstadisticaNombre", source = "tipoEstadistica.nombre")
        EstadisticaJugadorDTO toDTO(EstadisticaJugadorEvento estadistica);

        /**
         * Convierte una lista de entidades EstadisticaJugadorEvento a una lista de DTOs.
         *
         * @param estadisticas lista de entidades de origen
         * @return lista de DTOs de estadistica
         */
        List<EstadisticaJugadorDTO> toDTOList(List<EstadisticaJugadorEvento> estadisticas);
}
