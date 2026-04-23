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

/**
 * Mapper MapStruct para convertir entre entidades EquipoJugador y el DTO de jugador simple.
 * Gestiona la resolucion del dorsal efectivo: usa el dorsal especifico del equipo
 * si existe, y en caso contrario el dorsal general del jugador.
 *
 * @author Mario
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EquipoJugadorMapper {

    /**
     * Convierte una entidad EquipoJugador a un DTO simple de jugador.
     * Extrae los datos del jugador vinculado y resuelve el dorsal mediante
     * expresion Java: prioriza {@code dorsalEquipo} sobre el dorsal general del jugador.
     *
     * @param equipoJugador entidad de relacion equipo-jugador de origen
     * @return DTO simple del jugador con los datos en el contexto del equipo
     */
    @Mapping(target = "id", source = "jugador.id")
    @Mapping(target ="nombre", source = "jugador.nombre")
    @Mapping(target ="apellidos", source = "jugador.apellidos")
    @Mapping(target ="dorsal",
            expression = "java(equipoJugador.getDorsalEquipo() != null ? " +
                    "equipoJugador.getDorsalEquipo() : " +
                    "equipoJugador.getJugador().getDorsal())")
    @Mapping(target ="posicion", source = "jugador.posicion")
    @Mapping(target ="fotoUrl", source = "jugador.fotoUrl")
    JugadorSimpleDTO toJugadorSimpleDTO(EquipoJugador equipoJugador);

    /**
     * Convierte un conjunto de entidades EquipoJugador a una lista de DTOs simples de jugador.
     * Filtra unicamente los jugadores con estado activo antes de mapear.
     * Devuelve {@code null} si el conjunto de entrada es nulo.
     *
     * @param equipoJugadores conjunto de relaciones equipo-jugador de origen
     * @return lista de DTOs simples de los jugadores activos, o null si el conjunto es nulo
     */
    default List<JugadorSimpleDTO> toJugadorSimpleDTOList(Set<EquipoJugador> equipoJugadores){
        if (equipoJugadores == null){
            return null;
        }
        return equipoJugadores.stream()
                .filter(EquipoJugador::isActivo)
                .map(this::toJugadorSimpleDTO)
                .collect(Collectors.toList());
    }
}
