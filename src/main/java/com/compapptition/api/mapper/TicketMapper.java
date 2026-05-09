package com.compapptition.api.mapper;

import com.compapptition.api.dto.ticket.TicketDetalleDTO;
import com.compapptition.api.dto.ticket.TicketSimpleDTO;
import com.compapptition.api.entity.Ticket;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct entre la entidad {@link Ticket} y sus DTOs de proyección.
 * El estado del enum {@code EstadoTicket} se serializa a su nombre como
 * {@code String} para que el frontend reciba siempre un valor estable
 * sin acoplarse a los ordinales de Java.
 *
 * @author Mario
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketMapper {

    /**
     * Convierte un ticket a su DTO simple (listados).
     *
     * @param ticket entidad a mapear
     * @return DTO simple con datos básicos del autor y metadatos
     */
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioUsername", source = "usuario.username")
    @Mapping(target = "estado", expression = "java(ticket.getEstado() != null ? ticket.getEstado().name() : null)")
    TicketSimpleDTO toSimpleDTO(Ticket ticket);

    /**
     * Convierte un ticket a su DTO de detalle (descripción completa + email
     * del autor para que el admin pueda contactar fuera de la app si fuera
     * necesario).
     *
     * @param ticket entidad a mapear
     * @return DTO completo del ticket
     */
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioUsername", source = "usuario.username")
    @Mapping(target = "usuarioEmail", source = "usuario.email")
    @Mapping(target = "estado", expression = "java(ticket.getEstado() != null ? ticket.getEstado().name() : null)")
    TicketDetalleDTO toDetalleDTO(Ticket ticket);

    /**
     * Convierte una lista de tickets a DTOs simples para respuestas paginadas.
     *
     * @param tickets lista de entidades
     * @return lista de DTOs simples
     */
    List<TicketSimpleDTO> toSimpleDTOList(List<Ticket> tickets);
}
