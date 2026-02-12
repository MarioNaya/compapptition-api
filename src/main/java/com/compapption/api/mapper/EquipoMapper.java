package com.compapption.api.mapper;

import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.entity.Equipo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = EquipoJugadorMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EquipoMapper {

    // === ENTITY TO DTO === //

    // Equipo con todos los datos para uso detalle
    @Mapping(target = "numJugadores",
            expression = "java(equipo.getJugadores() != null ? " +
                    "(int) equipo.getJugadores().stream()" +
                    ".filter(ej -> ej.isActivo()).count() : 0)")
    @Mapping(target = "jugadores", source = "jugadores")
    EquipoDetalleDTO toDetalleDTO(Equipo equipo);

    // Equipo con datos simplificados para uso en listas y vistas
    EquipoSimpleDTO toSimpleDTO(Equipo equipo);

    // Listas equipos con los 2 formatos
    List<EquipoDetalleDTO> toDetalleDTOList(List<Equipo> equipos);
    List<EquipoSimpleDTO> toSimpleDTOList(List<Equipo> equipos);

    // Metodo para convertir byte[] a String Base64
    default String map(byte[] escudo) {
        if (escudo == null || escudo.length == 0) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(escudo);
    }
}