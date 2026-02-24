package com.compapption.api.mapper;

import com.compapption.api.dto.deporteDTO.DeporteDetalleDTO;
import com.compapption.api.dto.deporteDTO.DeporteSimpleDTO;
import com.compapption.api.entity.Deporte;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = TipoEstadisticaMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeporteMapper {

    // === ENTITY TO DTO === //

    // Deporte con todos los datos para uso en detalle
    @Mapping(target = "tipoEstadisticas", source = "tipoEstadisticaSet")
    DeporteDetalleDTO toDetalleDTO(Deporte deporte);

    // Deporte simplificado para uso en vistas y listados
    DeporteSimpleDTO toSimpleDTO(Deporte deporte);

    // Listas DTO para los dos formatos
    List<DeporteDetalleDTO> toDetalleDTOList(List<Deporte> deportes);
    List<DeporteSimpleDTO> toSimpleDTOList(List<Deporte> deportes);
}
