package com.compapption.api.mapper;

import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.entity.EquipoJugador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EquipoJugadorMapper {

    // Convierte EquipoJugador a JugadorSimpleDTO
    @Mapping(target = "id", source = "jugador.id")
    @Mapping(target ="nombre", source = "jugador.nombre")
    @Mapping(target ="apellidos", source = "jugador.apellidos")
    @Mapping(target ="dorsal",
            expression = "java(equipoJugador.getDorsalEquipo() != null ? " +
                    "equipoJugador.getDorsalEquipo() : " +
                    "equipoJugador.getJugador().getDorsal())")
    @Mapping(target ="posicion", source = "jugador.posicion")
    @Mapping(target ="foto", source = "jugador.foto")
    JugadorSimpleDTO toJugadorSimpleDTO(EquipoJugador equipoJugador);

    // Convierte Set de EquipoJugador a List de JugadorSimpleDTO
    default List<JugadorSimpleDTO> toJugadorSimpleDTOList(Set<EquipoJugador> equipoJugadores){
        if (equipoJugadores == null){
            return null;
        }
        return equipoJugadores.stream()
                .filter(EquipoJugador::getActivo)
                .map(this::toJugadorSimpleDTO)
                .collect(Collectors.toList());
    }
}
