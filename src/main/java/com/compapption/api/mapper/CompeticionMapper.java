package com.compapption.api.mapper;

import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.dto.competicionDTO.CompeticionInfoDTO;
import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades Competicion y sus DTOs.
 * Gestiona la transformación de campos anidados (deporte, creador, configuracion)
 * y el cálculo de numEquipos activos mediante expresión Java.
 *
 * @author Mario
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompeticionMapper {

    // === ENTITY TO DTO === //

    /**
     * Convierte una entidad Competicion a su DTO de detalle completo.
     * Mapea los campos anidados de deporte y creador, transforma la configuracion
     * mediante el método auxiliar {@code toConfiguracionDTO} y calcula
     * el numero de equipos activos con una expresión Java.
     *
     * @param competicion entidad de origen
     * @return DTO con todos los datos de la competicion, incluyendo configuracion
     */
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

    /**
     * Convierte una entidad Competicion a su DTO de informacion intermedia.
     * Incluye deporte, creador y el numero de equipos activos, pero omite
     * la configuracion detallada del formato.
     *
     * @param competicion entidad de origen
     * @return DTO con informacion resumida de la competicion
     */
    @Mapping(target = "deporteNombre", source = "deporte.nombre")
    @Mapping(target = "creadorUsername", source = "creador.username")
    @Mapping(target = "numEquipos",
            expression = "java(competicion.getEquipos() != null ? " +
                    "(int) competicion.getEquipos().stream()" +
                    ".filter(e -> e.isActivo()).count() : 0)")
    CompeticionInfoDTO toInfoDTO(Competicion competicion);

    /**
     * Convierte una entidad Competicion a su DTO simple para listados.
     * Incluye unicamente el nombre del deporte como campo anidado.
     *
     * @param competicion entidad de origen
     * @return DTO con los datos minimos de la competicion
     */
    @Mapping(target = "deporteNombre", source = "deporte.nombre")
    CompeticionSimpleDTO toSimpleDTO(Competicion competicion);

    /**
     * Convierte una lista de entidades Competicion a una lista de DTOs de detalle.
     *
     * @param competiciones lista de entidades de origen
     * @return lista de DTOs de detalle
     */
    List<CompeticionDetalleDTO> toDetalleDTOList(List<Competicion> competiciones);

    /**
     * Convierte una lista de entidades Competicion a una lista de DTOs de informacion intermedia.
     *
     * @param competiciones lista de entidades de origen
     * @return lista de DTOs de informacion
     */
    List<CompeticionInfoDTO> toInfoDTOList(List<Competicion> competiciones);

    /**
     * Convierte una lista de entidades Competicion a una lista de DTOs simples.
     *
     * @param competiciones lista de entidades de origen
     * @return lista de DTOs simples
     */
    List<CompeticionSimpleDTO> toSimpleDTOList(List<Competicion> competiciones);

    /**
     * Convierte una entidad ConfiguracionCompeticion al DTO de configuracion anidado en CompeticionDetalleDTO.
     * Devuelve {@code null} si la configuracion de entrada es nula.
     *
     * @param config entidad de configuracion de origen
     * @return DTO con los parametros del formato de competicion, o null si config es null
     */
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
