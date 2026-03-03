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
 * Implementación del patrón Strategy que genera calendarios de eliminación directa (bracket).
 * <p>
 * Genera todas las rondas de golpe: la primera ronda con equipos reales (con seeding
 * o barajados), y las rondas posteriores con eventos placeholder cuyos equipos se
 * rellenan automáticamente cuando se registra el resultado de la ronda anterior.
 * Soporta partido único, ida/vuelta y series best-of N (3, 5, 7).
 * El tamaño del bracket se expande automáticamente a la siguiente potencia de 2;
 * los huecos sobrantes se tratan como "bye" (el equipo pasa directamente).
 * </p>
 *
 * @author Mario
 */
@Component
public class GeneradorPlayoff implements GeneradorCalendario {

    /**
     * {@inheritDoc}
     * Devuelve {@code true} únicamente para el formato {@code PLAYOFF}.
     */
    @Override
    public boolean soporta(ConfiguracionCompeticion.FormatoCompeticion formato) {
        return formato == ConfiguracionCompeticion.FormatoCompeticion.PLAYOFF;
    }

    /**
     * {@inheritDoc}
     * Baraja aleatoriamente los equipos antes de generar el bracket para evitar seeding fijo.
     */
    @Override
    public List<Evento> generar(Competicion competicion,
                                List<Equipo> equipos,
                                LocalDateTime fechaInicio,
                                Integer diasJornada,
                                ConfiguracionCompeticion config) {
        List<Equipo> barajados = new ArrayList<>(equipos);
        Collections.shuffle(barajados);
        int diasJornada_ = diasJornada != null ? diasJornada : (config != null && config.getDiasEntreJornadas() != null ? config.getDiasEntreJornadas() : 7);
        int partidosEliminatoria = config != null && config.getPartidosEliminatoria() != null ? config.getPartidosEliminatoria() : 1;
        return generarBracketCompleto(competicion, barajados, fechaInicio, diasJornada_, 1, partidosEliminatoria);
    }

    /**
     * Genera el bracket completo de eliminación directa a partir de equipos ya ordenados (seeded).
     * <p>
     * La ronda 1 se genera con equipos reales. Las rondas posteriores se generan como eventos
     * placeholder que referencian los eventos decisivos (último partido) de la ronda anterior,
     * de forma que el sistema puede rellenar los equipos automáticamente al registrar resultados.
     * El bracket se empareja respetando el seeding estándar (1 vs N, 2 vs N-1...) usando
     * la función {@link #generarOrdenBracket}.
     * </p>
     *
     * @param competicion          competición para la que se genera el bracket
     * @param equipos              lista de equipos ordenada por seeding (índice 0 = cabeza de serie 1)
     * @param fechaInicio          fecha y hora del primer partido
     * @param diasJornada          días entre rondas del bracket
     * @param rondaInicial         número de jornada asignado a la primera ronda del bracket
     * @param partidosEliminatoria número de partidos por eliminatoria:
     *                             1 = partido único, 2 = ida/vuelta, 3/5/7 = best-of
     * @return lista de entidades {@link Evento} generadas (sin persistir)
     */
    public List<Evento> generarBracketCompleto(Competicion competicion,
                                               List<Equipo> equipos,
                                               LocalDateTime fechaInicio,
                                               int diasJornada,
                                               int rondaInicial,
                                               int partidosEliminatoria) {
        int tamBracket = siguientePotenciaDe2(equipos.size());
        int totalRondas = (int) (Math.log(tamBracket) / Math.log(2));
        List<Integer> orden = generarOrdenBracket(tamBracket);

        List<Evento> todos = new ArrayList<>();

        // Ronda 1: generar cruces con equipos reales
        // Cada cruce produce 'partidosEliminatoria' eventos
        // Los "eventos decisivos" de cada cruce (el último de la serie) se guardan
        // para referenciarlos en la siguiente ronda
        List<Evento> eventosDedecisivosPorCruce = new ArrayList<>(); // último partido de cada cruce en ronda 1

        int numCruces = tamBracket / 2;
        for (int c = 0; c < numCruces; c++) {
            int s1 = orden.get(c * 2);
            int s2 = orden.get(c * 2 + 1);

            // Si alguna semilla no tiene equipo real, saltar el cruce (bye)
            if (s1 > equipos.size() || s2 > equipos.size()) {
                eventosDedecisivosPorCruce.add(null); // bye
                continue;
            }

            Equipo local = equipos.get(s1 - 1);
            Equipo visitante = equipos.get(s2 - 1);

            List<Evento> serie = generarSerie(competicion, local, visitante, null, null,
                    fechaInicio, diasJornada, rondaInicial, c, numCruces,
                    partidosEliminatoria, 1, totalRondas);
            todos.addAll(serie);
            eventosDedecisivosPorCruce.add(serie.get(serie.size() - 1));
        }

        // Rondas 2+: generar placeholders referenciando los decisivos de la ronda anterior
        List<Evento> decisivosRondaAnterior = eventosDedecisivosPorCruce;
        for (int ronda = 2; ronda <= totalRondas; ronda++) {
            List<Evento> decisivosEstaRonda = new ArrayList<>();
            int crucesEstaRonda = tamBracket / (int) Math.pow(2, ronda);

            for (int c = 0; c < crucesEstaRonda; c++) {
                Evento anteriorLocal = decisivosRondaAnterior.get(c * 2);
                Evento anteriorVisitante = decisivosRondaAnterior.get(c * 2 + 1);

                // Si alguno es null (bye), saltar
                if (anteriorLocal == null || anteriorVisitante == null) {
                    decisivosEstaRonda.add(null);
                    continue;
                }

                List<Evento> serie = generarSerie(competicion, null, null,
                        anteriorLocal, anteriorVisitante,
                        fechaInicio, diasJornada, rondaInicial, c, crucesEstaRonda,
                        partidosEliminatoria, ronda, totalRondas);
                todos.addAll(serie);
                decisivosEstaRonda.add(serie.get(serie.size() - 1));
            }
            decisivosRondaAnterior = decisivosEstaRonda;
        }

        return todos;
    }

    /**
     * Genera los partidos de una serie (1, 2 o N partidos) para un cruce.
     *
     * Para posicionar en el tiempo:
     *   - cada slot = fechaInicio + (slotGlobal * diasJornada)
     *   - slotGlobal = (ronda-1) * partidosEliminatoria + indexPartidoDentroSerie
     *     + offset para distribuir los cruces de una misma ronda en el tiempo
     *
     * Distribución de jornadas:
     *   - Todos los partidos del partido 1 de la ronda comparten jornada base de la ronda.
     *   - Partido 2 de la ronda (vuelta/segunda) → jornada base + 1, etc.
     *   - Se asigna la jornada = rondaInicial + (ronda-1)*partidosEliminatoria + (numPartido-1)
     */
    private List<Evento> generarSerie(Competicion competicion,
                                      Equipo local, Equipo visitante,
                                      Evento anteriorLocal, Evento anteriorVisitante,
                                      LocalDateTime fechaInicio, int diasJornada,
                                      int rondaInicial, int crucePosicion, int totalCruces,
                                      int partidosEliminatoria, int ronda, int totalRondas) {
        List<Evento> serie = new ArrayList<>();
        boolean esPlaceholder = (local == null);

        // Slot base de esta ronda dentro de todo el bracket
        int slotBaseRonda = (ronda - 1) * partidosEliminatoria;

        for (int numPartido = 1; numPartido <= partidosEliminatoria; numPartido++) {
            int slotGlobal = slotBaseRonda + (numPartido - 1);
            int jornada = rondaInicial + slotGlobal;
            LocalDateTime fecha = fechaInicio.plusDays((long) slotGlobal * diasJornada);

            String nombreRonda = nombreRonda(ronda, totalRondas);
            String obs;
            if (partidosEliminatoria == 1) {
                obs = "Eliminatoria - " + nombreRonda;
            } else {
                obs = "Eliminatoria - " + nombreRonda + " - Partido " + numPartido + " de " + partidosEliminatoria;
            }

            Evento evento;
            if (esPlaceholder) {
                // Para ida/vuelta: en el partido 2 (vuelta) se invierten los roles.
                // Invertimos anteriorLocal/Visitante para que el llenado automático
                // sea siempre: anteriorLocal → equipo local, anteriorVisitante → equipo visitante.
                Evento effAnteriorLocal = (partidosEliminatoria == 2 && numPartido == 2) ? anteriorVisitante : anteriorLocal;
                Evento effAnteriorVisitante = (partidosEliminatoria == 2 && numPartido == 2) ? anteriorLocal : anteriorVisitante;
                evento = CalendarioUtils.crearEventoPlaceholder(
                        competicion, jornada, fecha, effAnteriorLocal, effAnteriorVisitante,
                        partidosEliminatoria > 1 ? numPartido : null);
            } else {
                // En ida/vuelta: partido 2 invierte local y visitante
                Equipo localPartido = (partidosEliminatoria == 2 && numPartido == 2) ? visitante : local;
                Equipo visitantePartido = (partidosEliminatoria == 2 && numPartido == 2) ? local : visitante;
                evento = CalendarioUtils.crearEvento(
                        competicion, localPartido, visitantePartido, jornada, fecha,
                        partidosEliminatoria > 1 ? numPartido : null);
            }
            evento.setObservaciones(obs);
            serie.add(evento);
        }

        return serie;
    }

    /**
     * Nombre descriptivo de la ronda según posición desde la final.
     * totalRondas=3 → ronda 3=Final, ronda 2=Semifinal, ronda 1=Cuartos
     */
    private String nombreRonda(int ronda, int totalRondas) {
        int posDesdeFinale = totalRondas - ronda + 1;
        return switch (posDesdeFinale) {
            case 1 -> "Final";
            case 2 -> "Semifinal";
            case 3 -> "Cuartos de Final";
            case 4 -> "Octavos de Final";
            default -> "Ronda " + ronda;
        };
    }

    /**
     * Genera el bracket completo seeded sin barajado aleatorio de los equipos.
     * <p>
     * Método de conveniencia que mantiene compatibilidad con usos anteriores;
     * usa partido único por defecto ({@code partidosEliminatoria = 1}).
     * </p>
     *
     * @param competicion  competición para la que se genera el bracket
     * @param equipos      lista de equipos ordenada por seeding
     * @param fechaInicio  fecha y hora del primer partido
     * @param diasJornada  días entre rondas; {@code null} equivale a 7 días
     * @param rondaInicial número de jornada de inicio del bracket
     * @return lista de entidades {@link Evento} generadas (sin persistir)
     */
    public List<Evento> generarBracket(Competicion competicion,
                                       List<Equipo> equipos,
                                       LocalDateTime fechaInicio,
                                       Integer diasJornada,
                                       int rondaInicial) {
        int dias = diasJornada != null ? diasJornada : 7;
        return generarBracketCompleto(competicion, equipos, fechaInicio, dias, rondaInicial, 1);
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
