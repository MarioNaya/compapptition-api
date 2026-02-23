package com.compapption.api.service.calendario;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import com.compapption.api.entity.Equipo;
import com.compapption.api.entity.Evento;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Algoritmo de eliminación directa (bracket).
 * Soporta: PLAYOFF (sorteo aleatorio en primera ronda).
 * El metodo generarBracket es público para reutilización desde CalendarioService
 * cuando se genera la fase eliminatoria tras una liga (equipos ya clasificados/seeded).
 */
@Component
public class GeneradorPlayoff implements GeneradorCalendario {

    @Override
    public boolean soporta(ConfiguracionCompeticion.FormatoCompeticion formato) {
        return formato == ConfiguracionCompeticion.FormatoCompeticion.PLAYOFF;
    }

    @Override
    public List<Evento> generar(Competicion competicion,
                                List<Equipo> equipos,
                                LocalDateTime fechaInicio,
                                Integer diasJornada,
                                ConfiguracionCompeticion config) {
        // Sorteo aleatorio (no seeded) para playoff directo
        List<Equipo> barajados = new ArrayList<>(equipos);
        Collections.shuffle(barajados);
        return generarBracket(competicion, barajados, fechaInicio, diasJornada, 1);
    }

    /**
     * Genera la primera ronda del bracket a partir de equipos ya ordenados.
     * Si seeded (por clasificación): 1vs8, 2vs7, 3vs6, 4vs5 — garantiza que
     * el 1 y el 2 no se crucen hasta la final.
     * Las rondas siguientes se generan cuando se conocen los ganadores.
     *
     * @param rondaInicial número de jornada asignado a esta ronda (útil en LIGA_PLAYOFF)
     */
    public List<Evento> generarBracket(Competicion competicion,
                                       List<Equipo> equipos,
                                       LocalDateTime fechaInicio,
                                       Integer diasjornada,
                                       int rondaInicial) {
        int tamBracket = siguientePotenciaDe2(equipos.size());
        List<Integer> orden = generarOrdenBracket(tamBracket);

        List<Evento> eventos = new ArrayList<>();
        for (int i = 0; i < orden.size(); i += 2) {
            int s1 = orden.get(i);
            int s2 = orden.get(i + 1);
            if (s1 > equipos.size() || s2 > equipos.size()) continue;

            Evento evento = CalendarioUtils.crearEvento(
                    competicion,
                    equipos.get(s1 -1),
                    equipos.get(s2 -1),
                    rondaInicial,
                    fechaInicio);
            evento.setObservaciones("Eliminatoria - Ronda " + rondaInicial);
            eventos.add(evento);
        }
        return eventos;
    }

    /**
     * Genera el orden del bracket recursivamente.
     * Para n=8: [1,8, 4,5, 2,7, 3,6]
     */
    private List<Integer> generarOrdenBracket(int n) {
        if (n == 1) return List.of(1);
        List<Integer> mitad = generarOrdenBracket(n / 2);
        List<Integer> resultado = new ArrayList<>();
        for (int semilla : mitad) {
            resultado.add(semilla);
            resultado.add(n + 1 - semilla);
        }
        return resultado;
    }

    private int siguientePotenciaDe2(int n) {
        int potencia = 1;
        while (potencia < n) potencia *= 2;
        return potencia;
    }
}
