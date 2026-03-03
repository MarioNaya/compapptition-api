package com.compapption.api.mapper;

import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import com.compapption.api.entity.TipoEstadistica;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades TipoEstadistica y su DTO.
 * Utilizado tambien por {@link DeporteMapper} para mapear la coleccion de tipos
 * de estadistica de un deporte.
 *
 * @author Mario
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TipoEstadisticaMapper {

    /**
     * Convierte una entidad TipoEstadistica a su DTO.
     *
     * @param tipoEstadistica entidad de origen
     * @return DTO con los datos del tipo de estadistica
     */
    TipoEstadisticaDTO toTipoEstadisticaDTO(TipoEstadistica tipoEstadistica);

    /**
     * Convierte una lista de entidades TipoEstadistica a una lista de DTOs.
     *
     * @param tipos lista de entidades de origen
     * @return lista de DTOs de tipo de estadistica
     */
    List<TipoEstadisticaDTO> toTipoEstadisticaDTOList(List<TipoEstadistica> tipos);
}
