package com.compapption.api.mapper;

import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.dto.competicionDTO.CompeticionInfoDTO;
import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompeticionMapper {

    // === ENTITY TO DTO === //

    // Detalle completo de competición
    @Mapping(target = "deporteId", source = "deporte.id")
    @Mapping(target = "deporteNombre", source = "deporte.nombre")
    @Mapping(target = "creadorId", source = "creador.id")
    @Mapping(target = "creadorUsername", source = "creador.username")
    @Mapping(target = "configuracion", source = "configuracion", qualifiedByName = "toConfiguracionDTO")
    @Mapping(target = "numEquipos",
            expression = "java(competicion.getEquipos() != null ? " +
            "(int) competicion.getEquipos().stream()" +
            ".filter(e -> e.isActivo()).count() : 0)")
    CompeticionDetalleDTO toDetalleDTO(Competicion competicion);

    // Información detalle intermedio de competición
    @Mapping(target = "deporteNombre", source = "deporte.nombre")
    @Mapping(target = "creadorUsername", source = "creador.username")
    @Mapping(target = "numEquipos",
            expression = "java(competicion.getEquipos() != null ? " +
                    "(int) competicion.getEquipos().stream()" +
                    ".filter(e -> e.isActivo()).count() : 0)")
    CompeticionInfoDTO toInfoDTO(Competicion competicion);

    // Formato simple información para listados
    @Mapping(target = "deporteNombre", source = "deporte.nombre")
    CompeticionSimpleDTO toSimpleDTO(Competicion competicion);

    // Listas de competición en los 3 formatos
    List<CompeticionDetalleDTO> toDetalleDTOList(List<Competicion> competiciones);
    List<CompeticionInfoDTO> toInfoDTOList(List<Competicion> competiciones);
    List<CompeticionSimpleDTO> toSimpleDTOList(List<Competicion> competiciones);

    // Contructor de DTO para configuración de competición
    @Named("toConfiguracionDTO")
    default CompeticionDetalleDTO.ConfiguracionDTO toConfiguracionDTO(ConfiguracionCompeticion config) {
        if (config == null){
            return null;
        }
        return CompeticionDetalleDTO.ConfiguracionDTO.builder()
                .puntosVictoria(config.getPuntosVictoria())
                .puntosEmpate(config.getPuntosEmpate())
                .puntosDerrota(config.getPuntosDerrota())
                .diasEntreJornada(config.getDiasEntreJornadas())
                .formato(config.getFormato())
                .numEquiposPlayoff(config.getNumEquiposPlayoff())
                .partidosEliminatoria(config.getPartidosEliminatoria())
                .build();
    }
}
