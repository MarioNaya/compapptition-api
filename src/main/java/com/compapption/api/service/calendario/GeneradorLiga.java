package com.compapption.api.service.calendario;

import com.compapption.api.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del patrón Strategy que genera calendarios mediante el algoritmo
 * de rotación circular (round-robin).
 * <p>
 * Soporta los formatos {@code LIGA} (solo ida), {@code LIGA_IDA_VUELTA} (ida y vuelta)
 * y {@code LIGA_PLAYOFF} (fase de liga previa al playoff). Cuando el número de equipos
 * es impar, añade un equipo fantasma ({@code null}) para que cada equipo descanse
 * una jornada. El método {@link #generarRoundRobin} es público para su reutilización
 * desde {@link GeneradorGruposPlayoff}.
 * </p>
 *
 * @author Mario
 */
@Component
public class GeneradorLiga implements GeneradorCalendario{


    /**
     * {@inheritDoc}
     * Devuelve {@code true} para {@code LIGA}, {@code LIGA_IDA_VUELTA} y {@code LIGA_PLAYOFF}.
     */
    @Override
    public boolean soporta(ConfiguracionCompeticion.FormatoCompeticion formato) {
        return formato == ConfiguracionCompeticion.FormatoCompeticion.LIGA
                || formato == ConfiguracionCompeticion.FormatoCompeticion.LIGA_IDA_VUELTA
                || formato == ConfiguracionCompeticion.FormatoCompeticion.LIGA_PLAYOFF;
    }

    /**
     * {@inheritDoc}
     * Determina si el formato requiere vuelta y delega en {@link #generarRoundRobin}.
     */
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
     * Genera todos los eventos mediante el algoritmo de rotación circular (round-robin).
     * <p>
     * Fija el primer equipo de la lista y rota los demás en cada jornada, garantizando
     * que cada par de equipos se enfrenta exactamente una vez en la fase de ida.
     * Si el número de equipos es impar, añade un equipo fantasma ({@code null}) que
     * actúa como descanso; los partidos contra ese equipo se omiten.
     * Si {@code idaYVuelta} es {@code true}, se generan las jornadas de vuelta
     * invirtiendo los roles de local y visitante respecto a la ida.
     * </p>
     *
     * @param competicion  competición para la que se generan los eventos
     * @param equipos      lista de equipos participantes
     * @param fechaInicio  fecha y hora de la primera jornada
     * @param diasJornada  número de días entre jornadas consecutivas
     * @param idaYVuelta   {@code true} para generar también la segunda vuelta
     * @return lista de entidades {@link Evento} generadas (sin persistir)
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
