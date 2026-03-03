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

/**
 * Mapper MapStruct para convertir entre entidades Evento y sus DTOs.
 * Gestiona la separacion del conjunto de EventoEquipo en equipo local y visitante
 * mediante los metodos auxiliares {@code extractLocal} y {@code extractVisitante},
 * y mapea las referencias al bracket de playoff (partidoAnteriorLocal/Visitante).
 *
 * @author Mario
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventoMapper {

    /**
     * Convierte una entidad EventoEquipo a su DTO de equipo participante en el evento.
     * Extrae el id, nombre y escudo del equipo, y el flag de local.
     *
     * @param eventoEquipo entidad de relacion evento-equipo de origen
     * @return DTO con los datos del equipo en el contexto del evento
     */
    @Mapping(target = "id",      source = "equipo.id")
    @Mapping(target = "nombre",  source = "equipo.nombre")
    @Mapping(target = "escudo",  source = "equipo.escudo")
    @Mapping(target = "esLocal", source = "esLocal")
    EventoEquipoDTO toEquipoDTO(EventoEquipo eventoEquipo);

    /**
     * Convierte una entidad Evento a su DTO de resultado.
     * Incluye unicamente el id de la competicion como campo anidado.
     *
     * @param evento entidad de origen
     * @return DTO con el resultado del evento
     */
    @Mapping(target = "competicionId", source = "competicion.id")
    EventoResultadoDTO toResultadoDTO(Evento evento);

    /**
     * Extrae el equipo local del conjunto de EventoEquipo del evento.
     * Filtra el primer elemento con {@code esLocal == true} y lo convierte a DTO.
     * Devuelve {@code null} si el conjunto es nulo o no hay equipo local.
     *
     * @param equipos conjunto de relaciones evento-equipo
     * @return DTO del equipo local, o null si no existe
     */
    @Named("extractLocal")
    default EventoEquipoDTO extractLocal(Set<EventoEquipo> equipos) {
        if (equipos == null) return null;
        return equipos.stream()
                .filter(EventoEquipo::isEsLocal)
                .findFirst()
                .map(this::toEquipoDTO)
                .orElse(null);
    }

    /**
     * Extrae el equipo visitante del conjunto de EventoEquipo del evento.
     * Filtra el primer elemento con {@code esLocal == false} y lo convierte a DTO.
     * Devuelve {@code null} si el conjunto es nulo o no hay equipo visitante.
     *
     * @param equipos conjunto de relaciones evento-equipo
     * @return DTO del equipo visitante, o null si no existe
     */
    @Named("extractVisitante")
    default EventoEquipoDTO extractVisitante(Set<EventoEquipo> equipos) {
        if (equipos == null) return null;
        return equipos.stream()
                .filter(ee -> !ee.isEsLocal())
                .findFirst()
                .map(this::toEquipoDTO)
                .orElse(null);
    }

    /**
     * Convierte una entidad Evento a su DTO de detalle completo.
     * Separa los equipos en local y visitante, mapea competicion, nombre y las
     * referencias al bracket de playoff (partidoAnteriorLocalId, partidoAnteriorVisitanteId).
     *
     * @param evento entidad de origen
     * @return DTO con todos los datos del evento, incluyendo referencias al bracket
     */
    @Mapping(target = "competicionId",              source = "competicion.id")
    @Mapping(target = "competicionNombre",          source = "competicion.nombre")
    @Mapping(target = "equipoLocal",                source = "equipos", qualifiedByName = "extractLocal")
    @Mapping(target = "equipoVisitante",            source = "equipos", qualifiedByName = "extractVisitante")
    @Mapping(target = "partidoAnteriorLocalId",     source = "partidoAnteriorLocal.id")
    @Mapping(target = "partidoAnteriorVisitanteId", source = "partidoAnteriorVisitante.id")
    EventoDetalleDTO toDetalleDTO(Evento evento);

    /**
     * Convierte una entidad Evento a su DTO simple para listados.
     * Incluye competicion y la separacion de equipos en local y visitante.
     *
     * @param evento entidad de origen
     * @return DTO con los datos resumidos del evento
     */
    @Mapping(target = "competicionId",     source = "competicion.id")
    @Mapping(target = "competicionNombre", source = "competicion.nombre")
    @Mapping(target = "equipoLocal",       source = "equipos", qualifiedByName = "extractLocal")
    @Mapping(target = "equipoVisitante",   source = "equipos", qualifiedByName = "extractVisitante")
    EventoSimpleDTO toSimpleDTO(Evento evento);

    /**
     * Convierte una lista de entidades Evento a una lista de DTOs de detalle.
     *
     * @param eventos lista de entidades de origen
     * @return lista de DTOs de detalle
     */
    List<EventoDetalleDTO> toDetalleDTOList(List<Evento> eventos);

    /**
     * Convierte una lista de entidades Evento a una lista de DTOs simples.
     *
     * @param eventos lista de entidades de origen
     * @return lista de DTOs simples
     */
    List<EventoSimpleDTO>  toSimpleDTOList(List<Evento> eventos);
}