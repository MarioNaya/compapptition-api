package com.compapption.api.mapper;

import com.compapption.api.dto.clasificacionDTO.ClasificacionDetalleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionSimpleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionUpdateDTO;
import com.compapption.api.entity.Clasificacion;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClasificacionMapper {

    // === ENTITY TO DTO === //

    // Detalle completo de clasificación
    @Mapping(target = "equipoNombre", source = "equipo.nombre")
    @Mapping(target = "equipoEscudo", source = "equipo.escudo")
    @Mapping(target = "competicionId", source = "competicion.id")
    @Mapping(target = "equipoId", source = "equipo.id")
    ClasificacionDetalleDTO toDetalleDTO(Clasificacion clasificacion);

    // Clasificación con información simplificada
    @Mapping(target = "equipoNombre", source = "equipo.nombre")
    @Mapping(target = "competicionId", source = "competicion.id")
    @Mapping(target = "equipoId", source = "equipo.id")
    ClasificacionSimpleDTO toSimpleDTO(Clasificacion clasificacion);

    // Listas
    List<ClasificacionDetalleDTO> toDetalleDTOList(List<Clasificacion> clasificaciones);
    List<ClasificacionSimpleDTO> toSimpleDTOList(List<Clasificacion> clasificaciones);

    // === DTO TO ENTITY === //

    // Para actualización (solo actualiza los campos no nulos)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "competicion", ignore = true)
    @Mapping(target = "equipo", ignore = true)
    void updateEntityFromDTO(ClasificacionUpdateDTO dto, @MappingTarget Clasificacion entity);

    // Método para convertir byte[] a String Base64
    default String map(byte[] escudo) {
        if (escudo == null || escudo.length == 0) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(escudo);
    }
}