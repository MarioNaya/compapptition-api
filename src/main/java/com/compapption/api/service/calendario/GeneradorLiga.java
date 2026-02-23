package com.compapption.api.service.calendario;

import com.compapption.api.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Algoritmo round-robin (rotación circular).
 * Soporta: LIGA (solo ida), LIGA_IDA_VUELTA, LIGA_PLAYOFF (fase de liga).
 * El metodo generarRoundRobin es público para reutilización desde GeneradorGruposPlayoff.
 */
@Component
public class GeneradorLiga implements GeneradorCalendario{


    @Override
    public boolean soporta(ConfiguracionCompeticion.FormatoCompeticion formato) {
        return formato == ConfiguracionCompeticion.FormatoCompeticion.LIGA
                || formato == ConfiguracionCompeticion.FormatoCompeticion.LIGA_IDA_VUELTA
                || formato == ConfiguracionCompeticion.FormatoCompeticion.LIGA_PLAYOFF;
    }

    @Override
    public List<Evento> generar(Competicion competicion,
                                List<Equipo> equipos,
                                LocalDateTime fechaInicio,
                                Integer diasJornada,
                                ConfiguracionCompeticion config) {
        boolean idaYVuelta = config.getFormato() == ConfiguracionCompeticion.FormatoCompeticion.LIGA_IDA_VUELTA;
        return generarRoundRobin(competicion,equipos,fechaInicio, diasJornada,idaYVuelta);
    }

    /**
     * Algoritmo de rotación circular (round-robin).
     * Fija el primer equipo y rota los demás para generar jornadas sin repetir enfrentamientos.
     * Si el número de equipos es impar, añade un equipo fantasma (null = descanso).
     */
    public List<Evento> generarRoundRobin(Competicion competicion,
                                          List<Equipo> equipos,
                                          LocalDateTime fechaInicio,
                                          int diasJornada,
                                          boolean idaYVuelta) {
        List<Equipo> lista = new ArrayList<>(equipos);
        if (lista.size()%2!=0) {
            lista.add(null); // Equipo fantasma null para participantes impares = descanso
        }

        int n = lista.size();
        int totalJornadas = n-1;
        List<Evento> eventos = new ArrayList<>();
        LocalDateTime fechajornada = fechaInicio;

        // Ida
        for (int jornada = 0; jornada < totalJornadas; jornada++) {
            for (int i = 0; i < n/2; i++) {
                Equipo local = lista.get(i);
                Equipo visitante = lista.get(n - 1 - i);
                if (local!=null && visitante!=null) {
                    eventos.add(CalendarioUtils.crearEvento(
                            competicion,
                            local,
                            visitante,
                            jornada + 1,
                            fechajornada));
                }
            }
            // Rotar: fijo el primero, desplazo el resto
            Equipo ultimo = lista.remove(n - 1);
            lista.add(1, ultimo);
            fechajornada = fechajornada.plusDays(diasJornada);
        }

        // Vuelta: duplicar invirtiendo local/visitante
        if (idaYVuelta) {
            int jornadasIda = totalJornadas;
            for (int jornada = 0; jornada < jornadasIda; jornada++) {
                final int jornadaIda = jornada + 1;
                List<Evento> eventosJornada = eventos.stream()
                        .filter(e -> e.getJornada() == jornadaIda)
                        .toList();
                for (Evento original : eventosJornada) {
                    Equipo localOrig = obtenerEquipoLocal(original);
                    Equipo visitanteOrig = obtenerEquipoVisitante(original);
                    if (localOrig!=null && visitanteOrig!=null) {
                        eventos.add(CalendarioUtils.crearEvento(
                                competicion,
                                visitanteOrig,
                                localOrig,
                                jornadasIda + jornada + 1,
                                fechajornada));
                    }
                }
                fechajornada = fechajornada.plusDays(diasJornada);
            }
        }
        return eventos;
    }

    private Equipo obtenerEquipoLocal(Evento evento) {
        return evento.getEquipos().stream()
                .filter(EventoEquipo::isEsLocal)
                .map(EventoEquipo::getEquipo)
                .findFirst().orElse(null);
    }

    private Equipo obtenerEquipoVisitante(Evento evento) {
        return evento.getEquipos().stream()
                .filter(ee -> !ee.isEsLocal())
                .map(EventoEquipo::getEquipo)
                .findFirst().orElse(null);
    }
}
