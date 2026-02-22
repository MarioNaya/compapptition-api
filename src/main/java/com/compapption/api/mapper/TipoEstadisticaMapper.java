package com.compapption.api.mapper;

import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import com.compapption.api.entity.TipoEstadistica;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TipoEstadisticaMapper {

    TipoEstadisticaDTO toTipoEstadisticaDTO(TipoEstadistica tipoEstadistica);

    List<TipoEstadisticaDTO> toTipoEstadisticaDTOList(List<TipoEstadistica> tipos);
}
