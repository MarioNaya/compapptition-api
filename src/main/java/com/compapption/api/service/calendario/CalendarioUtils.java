package com.compapption.api.service.calendario;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Equipo;
import com.compapption.api.entity.Evento;
import com.compapption.api.entity.EventoEquipo;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clase de utilidades compartidas por todos los generadores de calendario.
 * <p>
 * Proporciona métodos de factoría estáticos para construir entidades {@link Evento}
 * y {@link EventoEquipo} con la configuración correcta, evitando duplicación de código
 * entre los distintos algoritmos de generación.
 * Esta clase es package-private y solo es accesible dentro del paquete {@code calendario}.
 * </p>
 *
 * @author Mario
 */
@NoArgsConstructor
final class CalendarioUtils {

    /**
     * Crea un evento con los equipos local y visitante asignados, sin número de partido.
     *
     * @param competicion competición a la que pertenece el evento
     * @param local       equipo que juega como local
     * @param visitante   equipo que juega como visitante
     * @param jornada     número de jornada al que pertenece el evento
     * @param fechaHora   fecha y hora programada del evento
     * @return entidad {@link Evento} con sus dos {@link EventoEquipo} añadidos
     */
    static Evento crearEvento(Competicion competicion,
                              Equipo local,
                              Equipo visitante,
                              int jornada,
                              LocalDateTime fechaHora) {
        return crearEvento(competicion, local, visitante, jornada, fechaHora, null);
    }

    /**
     * Crea un evento con los equipos local y visitante asignados e indica el número
     * de partido dentro de una serie eliminatoria.
     *
     * @param competicion   competición a la que pertenece el evento
     * @param local         equipo que juega como local
     * @param visitante     equipo que juega como visitante
     * @param jornada       número de jornada al que pertenece el evento
     * @param fechaHora     fecha y hora programada del evento
     * @param numeroPartido número de partido dentro de la serie (p.ej. 1=ida, 2=vuelta);
     *                      {@code null} para partido único
     * @return entidad {@link Evento} con sus dos {@link EventoEquipo} añadidos
     */
    static Evento crearEvento(Competicion competicion,
                              Equipo local,
                              Equipo visitante,
                              int jornada,
                              LocalDateTime fechaHora,
                              Integer numeroPartido) {
        Evento evento = Evento.builder()
                .competicion(competicion)
                .jornada(jornada)
                .fechaHora(fechaHora)
                .numeroPartido(numeroPartido)
                .estado(Evento.EstadoEvento.PROGRAMADO)
                .build();

        EventoEquipo eventoLocal = EventoEquipo.builder()
                .evento(evento)
                .equipo(local)
                .esLocal(true)
                .build();

        EventoEquipo eventoVisitante = EventoEquipo.builder()
                .evento(evento)
                .equipo(visitante)
                .esLocal(false)
                .build();

        evento.getEquipos().add(eventoLocal);
        evento.getEquipos().add(eventoVisitante);

        return evento;
    }

    /**
     * Crea un evento placeholder para rondas futuras del bracket eliminatorio.
     * <p>
     * No tiene equipos asignados en el momento de la creación; los equipos se
     * rellenan automáticamente cuando se registra el resultado de los partidos
     * de la ronda anterior ({@code anteriorLocal} y {@code anteriorVisitante}).
     * </p>
     *
     * @param competicion      competición a la que pertenece el evento
     * @param jornada          número de jornada asignado
     * @param fechaHora        fecha y hora programada del evento
     * @param anteriorLocal    evento decisivo del que saldrá el equipo local
     * @param anteriorVisitante evento decisivo del que saldrá el equipo visitante
     * @param numeroPartido    número de partido dentro de la serie; {@code null} para partido único
     * @return entidad {@link Evento} placeholder sin equipos asignados
     */
    static Evento crearEventoPlaceholder(Competicion competicion,
                                         int jornada,
                                         LocalDateTime fechaHora,
                                         Evento anteriorLocal,
                                         Evento anteriorVisitante,
                                         Integer numeroPartido) {
        return Evento.builder()
                .competicion(competicion)
                .jornada(jornada)
                .fechaHora(fechaHora)
                .partidoAnteriorLocal(anteriorLocal)
                .partidoAnteriorVisitante(anteriorVisitante)
                .numeroPartido(numeroPartido)
                .estado(Evento.EstadoEvento.PROGRAMADO)
                .build();
    }
}
