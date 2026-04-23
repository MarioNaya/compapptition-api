package com.compapption.api.mapper;

import com.compapption.api.dto.clasificacionDTO.ClasificacionDetalleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionSimpleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionUpdateDTO;
import com.compapption.api.entity.Clasificacion;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades Clasificacion y sus DTOs.
 * Mapea los campos anidados de equipo (nombre, URL del escudo, id) y soporta la
 * actualizacion parcial de la entidad desde un DTO de actualizacion.
 *
 * @author Mario
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClasificacionMapper {

    // === ENTITY TO DTO === //

    /**
     * Convierte una entidad Clasificacion a su DTO de detalle completo.
     * Mapea los campos anidados de equipo (nombre, URL del escudo, id) y el id de la competicion.
     *
     * @param clasificacion entidad de origen
     * @return DTO con todos los datos de la clasificacion, incluyendo la URL del escudo
     */
    @Mapping(target = "equipoNombre", source = "equipo.nombre")
    @Mapping(target = "equipoEscudoUrl", source = "equipo.escudoUrl")
    @Mapping(target = "competicionId", source = "competicion.id")
    @Mapping(target = "equipoId", source = "equipo.id")
    ClasificacionDetalleDTO toDetalleDTO(Clasificacion clasificacion);

    /**
     * Convierte una entidad Clasificacion a su DTO simple para listados.
     * Omite el escudo del equipo respecto al DTO de detalle.
     *
     * @param clasificacion entidad de origen
     * @return DTO con los datos resumidos de la clasificacion
     */
    @Mapping(target = "equipoNombre", source = "equipo.nombre")
    @Mapping(target = "competicionId", source = "competicion.id")
    @Mapping(target = "equipoId", source = "equipo.id")
    ClasificacionSimpleDTO toSimpleDTO(Clasificacion clasificacion);

    /**
     * Convierte una lista de entidades Clasificacion a una lista de DTOs de detalle.
     *
     * @param clasificaciones lista de entidades de origen
     * @return lista de DTOs de detalle
     */
    List<ClasificacionDetalleDTO> toDetalleDTOList(List<Clasificacion> clasificaciones);

    /**
     * Convierte una lista de entidades Clasificacion a una lista de DTOs simples.
     *
     * @param clasificaciones lista de entidades de origen
     * @return lista de DTOs simples
     */
    List<ClasificacionSimpleDTO> toSimpleDTOList(List<Clasificacion> clasificaciones);

    // === DTO TO ENTITY === //

    /**
     * Actualiza una entidad Clasificacion existente con los valores no nulos del DTO de actualizacion.
     * Ignora los campos id, competicion y equipo para preservar las relaciones originales.
     *
     * @param dto    DTO con los nuevos valores a aplicar
     * @param entity entidad destino que sera modificada en sitio
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "competicion", ignore = true)
    @Mapping(target = "equipo", ignore = true)
    void updateEntityFromDTO(ClasificacionUpdateDTO dto, @MappingTarget Clasificacion entity);
}