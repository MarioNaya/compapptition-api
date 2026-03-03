package com.compapption.api.service;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import com.compapption.api.request.competicion.CompeticionCreateRequest;
import com.compapption.api.request.competicion.CompeticionUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio auxiliar para la creación y actualización de la configuración de una competición.
 * <p>
 * Encapsula la lógica de construcción del objeto {@link ConfiguracionCompeticion}
 * a partir de los datos de la petición REST, aplicando solo los campos presentes
 * (actualización parcial). No gestiona persistencia directamente; la delega al
 * servicio padre ({@code CompeticionService}).
 * </p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class ConfiguracionCompeticionService {

    /**
     * Construye y devuelve un objeto {@link ConfiguracionCompeticion} a partir de la
     * petición de creación de competición, aplicando únicamente los campos presentes.
     * <p>
     * Si {@code configRequest} es {@code null} o algún campo es {@code null}, se utilizan
     * los valores por defecto definidos en la entidad.
     * </p>
     *
     * @param competicion   entidad de la competición a la que pertenece la configuración
     * @param configRequest datos de configuración recibidos en la petición de creación;
     *                      puede ser {@code null}
     * @return instancia de {@link ConfiguracionCompeticion} lista para persistir
     */
    public ConfiguracionCompeticion crear(
            Competicion competicion,
            CompeticionCreateRequest.ConfiguracionRequest configRequest) {

        ConfiguracionCompeticion.ConfiguracionCompeticionBuilder builder = ConfiguracionCompeticion
                .builder()
                .competicion(competicion);

        if (configRequest != null) {
            if (configRequest.getPuntosVictoria() != null) builder.puntosVictoria(configRequest.getPuntosVictoria());
            if (configRequest.getPuntosEmpate() != null)   builder.puntosEmpate(configRequest.getPuntosEmpate());
            if (configRequest.getPuntosDerrota() != null)  builder.puntosDerrota(configRequest.getPuntosDerrota());
            if (configRequest.getFormato() != null)        builder.formato(configRequest.getFormato());
            if (configRequest.getNumEquiposPlayoff() != null) builder.numEquiposPlayoff(configRequest.getNumEquiposPlayoff());
            if (configRequest.getPartidosEliminatoria() != null) builder.partidosEliminatoria(configRequest.getPartidosEliminatoria());
        }

        return builder.build();
    }

    /**
     * Actualiza de forma parcial una {@link ConfiguracionCompeticion} existente
     * con los campos no nulos de la petición de actualización.
     * <p>
     * Solo se modifican los campos explícitamente enviados en la petición;
     * los campos {@code null} se ignoran preservando el valor actual.
     * </p>
     *
     * @param config  entidad de configuración a actualizar (modificada in-place)
     * @param request datos de actualización; los campos {@code null} se omiten
     */
    public void actualizar(
            ConfiguracionCompeticion config,
            CompeticionUpdateRequest.ConfiguracionUpdateRequest request) {

        if (request.getPuntosVictoria() != null) config.setPuntosVictoria(request.getPuntosVictoria());
        if (request.getPuntosEmpate() != null)   config.setPuntosEmpate(request.getPuntosEmpate());
        if (request.getPuntosDerrota() != null)  config.setPuntosDerrota(request.getPuntosDerrota());
        if (request.getFormato() != null) {
            config.setFormato(request.getFormato());
        }
        if (request.getNumEquiposPlayOff() != null) {
            config.setNumEquiposPlayoff(request.getNumEquiposPlayOff());
        }
        if (request.getPartidosEliminatoria() != null) {
            config.setPartidosEliminatoria(request.getPartidosEliminatoria());
        }
    }
}
