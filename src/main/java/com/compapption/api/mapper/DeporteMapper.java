package com.compapption.api.mapper;

import com.compapption.api.dto.deporteDTO.DeporteDetalleDTO;
import com.compapption.api.dto.deporteDTO.DeporteSimpleDTO;
import com.compapption.api.entity.Deporte;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades Deporte y sus DTOs.
 * Utiliza {@link TipoEstadisticaMapper} para mapear la coleccion de tipos
 * de estadistica asociados al deporte en el formato detalle.
 *
 * @author Mario
 */
@Mapper(
        componentModel = "spring",
        uses = TipoEstadisticaMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeporteMapper {

    // === ENTITY TO DTO === //

    /**
     * Convierte una entidad Deporte a su DTO de detalle completo.
     * Mapea el conjunto de tipos de estadistica ({@code tipoEstadisticaSet})
     * al campo {@code tipoEstadisticas} del DTO, delegando en {@link TipoEstadisticaMapper}.
     *
     * @param deporte entidad de origen
     * @return DTO con todos los datos del deporte, incluyendo sus tipos de estadistica
     */
    @Mapping(target = "tipoEstadisticas", source = "tipoEstadisticaSet")
    DeporteDetalleDTO toDetalleDTO(Deporte deporte);

    /**
     * Convierte una entidad Deporte a su DTO simple para listados y vistas.
     *
     * @param deporte entidad de origen
     * @return DTO con los datos minimos del deporte
     */
    DeporteSimpleDTO toSimpleDTO(Deporte deporte);

    /**
     * Convierte una lista de entidades Deporte a una lista de DTOs de detalle.
     *
     * @param deportes lista de entidades de origen
     * @return lista de DTOs de detalle
     */
    List<DeporteDetalleDTO> toDetalleDTOList(List<Deporte> deportes);

    /**
     * Convierte una lista de entidades Deporte a una lista de DTOs simples.
     *
     * @param deportes lista de entidades de origen
     * @return lista de DTOs simples
     */
    List<DeporteSimpleDTO> toSimpleDTOList(List<Deporte> deportes);
}
