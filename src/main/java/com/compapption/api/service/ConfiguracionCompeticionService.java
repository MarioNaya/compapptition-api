package com.compapption.api.service;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import com.compapption.api.request.competicion.CompeticionCreateRequest;
import com.compapption.api.request.competicion.CompeticionUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfiguracionCompeticionService {

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
