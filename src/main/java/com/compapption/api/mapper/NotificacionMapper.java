package com.compapption.api.mapper;

import com.compapption.api.dto.notificacion.NotificacionDTO;
import com.compapption.api.entity.Notificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper manual para convertir entre {@link Notificacion} y {@link NotificacionDTO}.
 * <p>
 * Se implementa como {@code @Component} en lugar de MapStruct porque necesita
 * deserializar el campo {@code payloadJson} a un {@code Map<String, Object>}
 * mediante un {@link ObjectMapper} de Jackson, operación que queda más clara
 * escrita a mano.
 * </p>
 *
 * @author Mario
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    /**
     * Convierte una entidad {@link Notificacion} a su DTO, deserializando el
     * {@code payloadJson} a un mapa. Si el JSON es inválido o nulo, devuelve un mapa vacío.
     *
     * @param notificacion entidad de origen
     * @return DTO de la notificación
     */
    public NotificacionDTO toDTO(Notificacion notificacion) {
        if (notificacion == null) return null;

        Map<String, Object> payload = new HashMap<>();
        if (notificacion.getPayloadJson() != null && !notificacion.getPayloadJson().isBlank()) {
            try {
                payload = objectMapper.readValue(notificacion.getPayloadJson(), MAP_TYPE);
            } catch (Exception e) {
                log.warn("No se pudo deserializar payloadJson de notificacion id={}: {}",
                        notificacion.getId(), e.getMessage());
            }
        }

        return NotificacionDTO.builder()
                .id(notificacion.getId())
                .tipo(notificacion.getTipo() != null ? notificacion.getTipo().name() : null)
                .payload(payload)
                .leida(notificacion.isLeida())
                .fechaCreacion(notificacion.getFechaCreacion())
                .build();
    }

    /**
     * Convierte una lista de entidades a DTOs.
     *
     * @param notificaciones lista de entidades
     * @return lista de DTOs
     */
    public List<NotificacionDTO> toDTOList(List<Notificacion> notificaciones) {
        return notificaciones.stream().map(this::toDTO).toList();
    }
}
