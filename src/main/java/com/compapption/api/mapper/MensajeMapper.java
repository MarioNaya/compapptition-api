package com.compapption.api.mapper;

import com.compapption.api.dto.mensaje.MensajeDTO;
import com.compapption.api.entity.Mensaje;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct para convertir la entidad {@link Mensaje} a su DTO.
 * Extrae los campos anidados {@code conversacion.id} y {@code autor.id/username}
 * y deriva el flag {@code leido} a partir de {@code leidoAt != null}.
 *
 * @author Mario
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MensajeMapper {

    /**
     * Convierte una entidad {@link Mensaje} a su DTO. El flag {@code leido} se calcula
     * a partir de {@code leidoAt != null} mediante una expresión de MapStruct.
     *
     * @param mensaje entidad de origen
     * @return DTO con los datos del mensaje
     */
    @Mapping(target = "conversacionId", source = "conversacion.id")
    @Mapping(target = "autorId",        source = "autor.id")
    @Mapping(target = "autorUsername",  source = "autor.username")
    @Mapping(target = "leido",          expression = "java(mensaje.getLeidoAt() != null)")
    MensajeDTO toDTO(Mensaje mensaje);

    /**
     * Convierte una lista de mensajes a una lista de DTOs.
     *
     * @param mensajes lista de entidades
     * @return lista de DTOs
     */
    List<MensajeDTO> toDTOList(List<Mensaje> mensajes);
}
