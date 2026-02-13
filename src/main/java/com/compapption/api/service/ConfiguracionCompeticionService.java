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
            builder.puntosVictoria(configRequest.getPuntosVictoria())
                    .puntosEmpate(configRequest.getPuntosEmpate())
                    .puntosDerrota(configRequest.getPuntosDerrota())
                    .formato(configRequest.getFormato() != null ? configRequest.getFormato() : ConfiguracionCompeticion.FormatoCompeticion.LIGA)
                    .numEquiposPlayoff(configRequest.getNumEquiposPlayoff())
                    .partidosEliminatoria(configRequest.getPartidosEliminatoria());
        }

        return builder.build();
    }

    public void actualizar(
            ConfiguracionCompeticion config,
            CompeticionUpdateRequest.ConfiguracionUpdateRequest request) {

        config.setPuntosVictoria(request.getPuntosVictoria());
        config.setPuntosEmpate(request.getPuntosEmpate());
        config.setPuntosDerrota(request.getPuntosDerrota());
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
