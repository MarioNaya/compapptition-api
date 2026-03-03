package com.compapption.api.mapper;

import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.entity.Equipo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades Equipo y sus DTOs.
 * Gestiona la transformacion del escudo de {@code byte[]} a Base64 y
 * el calculo del numero de jugadores activos. Utiliza {@link EquipoJugadorMapper}
 * para mapear la coleccion de jugadores.
 *
 * @author Mario
 */
@Mapper(
        componentModel = "spring",
        uses = EquipoJugadorMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EquipoMapper {

    // === ENTITY TO DTO === //

    /**
     * Convierte una entidad Equipo a su DTO de detalle completo.
     * Calcula el numero de jugadores activos mediante expresion Java e incluye
     * la lista de jugadores delegando en {@link EquipoJugadorMapper}.
     *
     * @param equipo entidad de origen
     * @return DTO con todos los datos del equipo, incluyendo jugadores activos
     */
    @Mapping(target = "numJugadores",
            expression = "java(equipo.getJugadores() != null ? " +
                    "(int) equipo.getJugadores().stream()" +
                    ".filter(ej -> ej.isActivo()).count() : 0)")
    @Mapping(target = "jugadores", source = "jugadores")
    EquipoDetalleDTO toDetalleDTO(Equipo equipo);

    /**
     * Convierte una entidad Equipo a su DTO simple para listados y vistas.
     *
     * @param equipo entidad de origen
     * @return DTO con los datos minimos del equipo
     */
    EquipoSimpleDTO toSimpleDTO(Equipo equipo);

    /**
     * Convierte una lista de entidades Equipo a una lista de DTOs de detalle.
     *
     * @param equipos lista de entidades de origen
     * @return lista de DTOs de detalle
     */
    List<EquipoDetalleDTO> toDetalleDTOList(List<Equipo> equipos);

    /**
     * Convierte una lista de entidades Equipo a una lista de DTOs simples.
     *
     * @param equipos lista de entidades de origen
     * @return lista de DTOs simples
     */
    List<EquipoSimpleDTO> toSimpleDTOList(List<Equipo> equipos);

    /**
     * Convierte un array de bytes que representa un escudo a su representacion Base64.
     * Devuelve {@code null} si el array es nulo o vacio.
     *
     * @param escudo imagen del escudo en bytes
     * @return cadena Base64 del escudo, o null si no hay imagen
     */
    default String map(byte[] escudo) {
        if (escudo == null || escudo.length == 0) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(escudo);
    }
}