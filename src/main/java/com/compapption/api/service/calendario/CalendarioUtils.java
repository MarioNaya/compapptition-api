package com.compapption.api.service.calendario;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Equipo;
import com.compapption.api.entity.Evento;
import com.compapption.api.entity.EventoEquipo;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Utilidades compartidas por todos los generadores de calendario.
 * Package-private: solo accesible dentro del paquete calendario.
 */
@NoArgsConstructor
final class CalendarioUtils {

    static Evento crearEvento(Competicion competicion,
                              Equipo local,
                              Equipo visitante,
                              int jornada,
                              LocalDateTime fechaHora) {
        Evento evento = Evento.builder()
                .competicion(competicion)
                .jornada(jornada)
                .fechaHora(fechaHora)
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
}
