package com.compapption.api.service.calendario;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import com.compapption.api.entity.Equipo;
import com.compapption.api.entity.Evento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación del patrón Strategy que genera calendarios de fase de grupos seguida de playoff.
 * <p>
 * Distribuye los equipos en grupos equilibrados mediante un sistema de bombos y genera
 * un calendario round-robin para cada grupo (delegando en {@link GeneradorLiga}).
 * El número de grupos se calcula automáticamente a partir del total de equipos y del
 * número de clasificados para el playoff definido en la configuración.
 * La fase eliminatoria posterior se genera mediante
 * {@link com.compapption.api.service.CalendarioService#generarPlayoffSeededPorIdDetalle}.
 * </p>
 *
 * @author Mario
 */
@Component
@RequiredArgsConstructor
public class GeneradorGruposPlayoff implements GeneradorCalendario {

    private final GeneradorLiga generadorLiga;

    /**
     * {@inheritDoc}
     * Devuelve {@code true} únicamente para el formato {@code GRUPOS_PLAYOFF}.
     */
    @Override
    public boolean soporta(ConfiguracionCompeticion.FormatoCompeticion formato) {
        return formato == ConfiguracionCompeticion.FormatoCompeticion.GRUPOS_PLAYOFF;
    }

    /**
     * {@inheritDoc}
     * Calcula el número de grupos, sortea los equipos mediante bombos y genera el calendario
     * round-robin de la fase de grupos. Los eventos de cada grupo se etiquetan con
     * {@code "Grupo N"} en el campo observaciones.
     *
     * @throws IllegalStateException si hay menos de 6 equipos (no se pueden formar al menos 2 grupos de 3)
     */
    @Override
    public List<Evento> generar(Competicion competicion,
                                List<Equipo> equipos,
                                LocalDateTime fechaInicio,
                                Integer diasJornada,
                                ConfiguracionCompeticion config) {
        // Si el usuario ha fijado el número de grupos en la configuración, se
        // respeta (validado contra mínimos). Si no, se calcula automáticamente.
        int numGrupos = (config.getNumGrupos() != null)
                ? validarNumGrupos(config.getNumGrupos(), equipos.size())
                : calcularNumGrupos(equipos.size(), config.getNumEquiposPlayoff());
        List<List<Equipo>> grupos = sortearGrupos(equipos, numGrupos);

        List<Evento> eventos = new ArrayList<>();
        for (int g = 0; g < grupos.size(); g++) {
            List<Evento> eventosGrupo =
                    generadorLiga.generarRoundRobin(competicion,
                            grupos.get(g),
                            fechaInicio,
                            diasJornada,
                            false);
            final String etiqueta = "Grupo " + (g + 1);
            eventosGrupo.forEach(e -> e.setObservaciones(etiqueta));
            eventos.addAll(eventosGrupo);
        }
        return eventos;
    }

    /**
     * Distribuye equipos en grupos usando sistema de bombos.
     * Cada grupo recibe un equipo de cada bombo para equilibrio competitivo.
     */
    private List<List<Equipo>> sortearGrupos(List<Equipo> equipos, int numGrupos) {
        List<Equipo> barajados = new ArrayList<>(equipos);
        Collections.shuffle(barajados);

        int equiposPorGrupo = barajados.size() / numGrupos;

        // Crear bombos: equipo i del bombo k -> grupo k
        List<List<Equipo>> bombos = new ArrayList<>();
        for (int i = 0; i < equiposPorGrupo; i++) {
            int desde = i * numGrupos;
            int hasta = Math.min(desde + numGrupos, barajados.size());
            List<Equipo> bombo = new ArrayList<>(barajados.subList(desde,hasta));
            Collections.shuffle(bombo);
            bombos.add(bombo);
        }

        List<List<Equipo>> grupos = new ArrayList<>();
        for (int g = 0; g < numGrupos; g++) {
            List<Equipo> grupo = new ArrayList<>();
            for (List<Equipo> bombo : bombos) {
                if (g < bombo.size()) grupo.add(bombo.get(g));
            }
            grupos.add(grupo);
        }

        // Equipos sobrantes (si el total no es divisible exacto) al último grupo
        int asignados = equiposPorGrupo * numGrupos;
        for (int i = asignados; i < barajados.size(); i++) {
            grupos.get(i - asignados).add(barajados.get(i));
        }

        return grupos;
    }


    private int calcularNumGrupos(int numEquipos, int numEquiposPlayoff) {
        int maxGrupos = numEquipos / 3;
        if (maxGrupos < 2) {
            throw new IllegalStateException(
                    "Se necesitan al menos 6 equipos para formato de grupos (hay " + numEquipos + ")");
        }
        int numGrupos = Math.max(2, numEquiposPlayoff / 2);
        return Math.min(numGrupos, maxGrupos);
    }

    /**
     * Valida que el número de grupos solicitado por el usuario sea viable:
     * al menos 2 grupos y con al menos 3 equipos por grupo para que el
     * round-robin tenga sentido.
     *
     * @throws IllegalStateException si no cumple los límites.
     */
    private int validarNumGrupos(int numGrupos, int numEquipos) {
        if (numGrupos < 2) {
            throw new IllegalStateException(
                    "El número de grupos debe ser al menos 2 (solicitados: " + numGrupos + ")");
        }
        int maxGrupos = numEquipos / 3;
        if (numGrupos > maxGrupos) {
            throw new IllegalStateException(
                    "Con " + numEquipos + " equipos no se pueden formar " + numGrupos
                    + " grupos de al menos 3 equipos (máximo permitido: " + maxGrupos + ")");
        }
        return numGrupos;
    }

}
