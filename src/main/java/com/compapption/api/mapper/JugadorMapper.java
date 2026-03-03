package com.compapption.api.mapper;

import com.compapption.api.dto.jugadorDTO.JugadorDetalleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorUsuarioDTO;
import com.compapption.api.entity.Jugador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades Jugador y sus DTOs.
 * Gestiona la transformacion de campos del usuario vinculado (id y username)
 * en los formatos de detalle y vinculacion.
 *
 * @author Mario
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JugadorMapper {

    // === ENTITY TO DTO === //

    /**
     * Convierte una entidad Jugador a su DTO de detalle completo.
     * Mapea los campos del usuario vinculado (id y username).
     *
     * @param jugador entidad de origen
     * @return DTO con todos los datos del jugador, incluyendo datos del usuario
     */
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioUsername", source = "usuario.username")
    JugadorDetalleDTO toDetalleDTO(Jugador jugador);

    /**
     * Convierte una entidad Jugador a su DTO simple para listados y vistas.
     *
     * @param jugador entidad de origen
     * @return DTO con los datos minimos del jugador
     */
    JugadorSimpleDTO toSimpleDTO(Jugador jugador);

    /**
     * Convierte una entidad Jugador a su DTO de vinculacion con usuario.
     * Incluye el id y username del usuario asociado al jugador.
     *
     * @param jugador entidad de origen
     * @return DTO con los datos del jugador y su usuario vinculado
     */
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioUsername", source = "usuario.username")
    JugadorUsuarioDTO toUsuarioDTO(Jugador jugador);

    /**
     * Convierte una lista de entidades Jugador a una lista de DTOs de detalle.
     *
     * @param jugadores lista de entidades de origen
     * @return lista de DTOs de detalle
     */
    List<JugadorDetalleDTO> toDetalleDTOList(List<Jugador> jugadores);

    /**
     * Convierte una lista de entidades Jugador a una lista de DTOs simples.
     *
     * @param jugadores lista de entidades de origen
     * @return lista de DTOs simples
     */
    List<JugadorSimpleDTO> toSimpleDTOList(List<Jugador> jugadores);
}
