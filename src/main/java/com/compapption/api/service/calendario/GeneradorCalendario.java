package com.compapption.api.service.calendario;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import com.compapption.api.entity.Equipo;
import com.compapption.api.entity.Evento;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato del patrón Strategy para generación de calendarios.
 * Cada implementación encapsula el algoritmo de un formato de competición distinto.
 */
public interface GeneradorCalendario {

    boolean soporta(ConfiguracionCompeticion.FormatoCompeticion formato);

    List<Evento> generar(Competicion competicion,
                         List<Equipo> equipos,
                         LocalDateTime fechaInicio,
                         Integer diasJornada,
                         ConfiguracionCompeticion config);
}
