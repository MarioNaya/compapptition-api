package com.compapption.api.mapper;

import com.compapption.api.dto.jugadorDTO.JugadorDetalleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorUsuarioDTO;
import com.compapption.api.entity.Jugador;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JugadorMapper {

    // === ENTITY TO DTO === //

    // Jugador con todos los atributos
    JugadorDetalleDTO toDetalleDTO(Jugador jugador);

    // Jugador simplificado para uso en listas y vistas
    JugadorSimpleDTO toSimpleDTO(Jugador jugador);

    // Jugador para vinculación con usuario
    JugadorUsuarioDTO toUsuarioDTO(Jugador jugador);

    // Listas jugadores con los 2 formatos
    List<JugadorDetalleDTO> toDetalleDTOList(List<Jugador> jugadores);
    List<JugadorSimpleDTO> toSimpleDTOList(List<Jugador> jugadores);
}
