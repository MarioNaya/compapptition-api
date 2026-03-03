package com.compapption.api.service.calendario;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import com.compapption.api.entity.Equipo;
import com.compapption.api.entity.Evento;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato del patrón Strategy para la generación de calendarios de competición.
 * <p>
 * Cada implementación encapsula el algoritmo correspondiente a un formato de
 * competición ({@code LIGA}, {@code PLAYOFF}, {@code GRUPOS_PLAYOFF}, etc.).
 * El orquestador ({@link com.compapption.api.service.CalendarioService}) selecciona
 * en tiempo de ejecución la implementación que soporta el formato de la competición.
 * </p>
 *
 * @author Mario
 */
public interface GeneradorCalendario {

    /**
     * Indica si esta implementación es capaz de generar el calendario para el formato indicado.
     *
     * @param formato formato de competición a evaluar
     * @return {@code true} si la implementación soporta el formato
     */
    boolean soporta(ConfiguracionCompeticion.FormatoCompeticion formato);

    /**
     * Genera la lista de eventos del calendario para la competición indicada.
     * <p>
     * Los eventos devueltos no están persistidos; la persistencia la realiza el orquestador.
     * </p>
     *
     * @param competicion competición para la que se genera el calendario
     * @param equipos     lista de equipos participantes activos
     * @param fechaInicio fecha y hora del primer evento
     * @param diasJornada número de días entre jornadas consecutivas
     * @param config      configuración de la competición con los parámetros del formato
     * @return lista de entidades {@link Evento} generadas (sin persistir)
     */
    List<Evento> generar(Competicion competicion,
                         List<Equipo> equipos,
                         LocalDateTime fechaInicio,
                         Integer diasJornada,
                         ConfiguracionCompeticion config);
}
