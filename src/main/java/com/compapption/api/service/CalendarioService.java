package com.compapption.api.service;

import com.compapption.api.dto.eventoDTO.EventoDetalleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.InternalStateException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EventoMapper;
import com.compapption.api.repository.ClasificacionRepository;
import com.compapption.api.repository.CompeticionEquipoRepository;
import com.compapption.api.repository.CompeticionRepository;
import com.compapption.api.repository.EventoRepository;
import com.compapption.api.service.calendario.GeneradorCalendario;
import com.compapption.api.service.calendario.GeneradorPlayoff;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio orquestador del patrón Strategy para la generación de calendarios de competición.
 * <p>
 * En tiempo de ejecución selecciona el {@link GeneradorCalendario} que soporta
 * el formato de la competición ({@code LIGA}, {@code LIGA_IDA_VUELTA}, {@code PLAYOFF},
 * {@code GRUPOS_PLAYOFF}) e invoca su algoritmo. Persiste los eventos generados
 * y expone dos niveles de API: métodos fachada que aceptan IDs y devuelven DTOs
 * (para los controllers) y métodos de dominio que trabajan con entidades
 * (para otros servicios).
 * </p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class CalendarioService {

    private final EventoRepository eventoRepository;
    private final CompeticionEquipoRepository competicionEquipoRepository;
    private final CompeticionRepository competicionRepository;
    private final ClasificacionRepository clasificacionRepository;
    private final EventoMapper eventoMapper;

    /** Spring inyecta automáticamente todas las implementaciones de GeneradorCalendario. */
    private final List<GeneradorCalendario> generadores;

    /** Inyección directa para la fase de playoff seeded (uso explícito del bracket). */
    private final GeneradorPlayoff generadorPlayoff;

    /// === MÉTODOS FACHADA PARA EL CONTROLLER (ACEPTAN IDs Y DEVUELVEN DTOS === ///

    /**
     * Genera el calendario completo de una competición según su formato y lo persiste.
     * <p>
     * Método fachada orientado al controller: recibe IDs y devuelve DTOs. Delega
     * la lógica real en {@link #generarCalendario(Competicion, LocalDateTime, Integer)}.
     * </p>
     *
     * @param competicionId  identificador de la competición
     * @param fechaInicio    fecha y hora del primer evento del calendario
     * @param diasJornada    número de días entre jornadas consecutivas
     * @return lista de DTOs detalle de los eventos generados
     * @throws com.compapption.api.exception.ResourceNotFoundException si la competición no existe
     * @throws com.compapption.api.exception.BadRequestException       si la competición no tiene
     *                                                                  configuración o hay menos de
     *                                                                  2 equipos activos
     */
    @Transactional
    public List<EventoDetalleDTO> generarCalendarioPorIdDetalle(Long competicionId, LocalDateTime fechaInicio, Integer diasJornada) {
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", competicionId));
        return eventoMapper.toDetalleDTOList(generarCalendario(competicion,fechaInicio,diasJornada));
    }

    /**
     * Genera la fase eliminatoria (playoff seeded) para los formatos {@code LIGA_PLAYOFF}
     * o {@code GRUPOS_PLAYOFF} y la persiste.
     * <p>
     * Toma los mejores N equipos de la clasificación actual según
     * {@code ConfiguracionCompeticion.numEquiposPlayoff}. Si {@code rondaInicial} es
     * {@code null}, se calcula como {@code maxJornada + 1} para continuar la numeración
     * del calendario de liga. Los parámetros opcionales se leen de la configuración
     * si no se proporcionan.
     * </p>
     *
     * @param competicionId identificador de la competición
     * @param fechaInicio   fecha y hora del primer partido de la fase eliminatoria
     * @param rondaInicial  número de jornada asignado a la primera ronda; {@code null} para
     *                      calcular automáticamente
     * @param diasJornada   días entre rondas; {@code null} para usar el valor de la configuración
     * @return lista de DTOs detalle de los eventos del playoff generados
     * @throws com.compapption.api.exception.ResourceNotFoundException si la competición no existe
     * @throws com.compapption.api.exception.BadRequestException       si no hay equipos clasificados
     */
    @Transactional
    public List<EventoDetalleDTO> generarPlayoffSeededPorIdDetalle(Long competicionId,
                                                            LocalDateTime fechaInicio,
                                                            Integer rondaInicial,
                                                            Integer diasJornada) {
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));

        ConfiguracionCompeticion config = competicion.getConfiguracion();
        int numEquiposPlayoff = config != null ? config.getNumEquiposPlayoff() : 8;
        int dias = diasJornada != null ? diasJornada : (config != null && config.getDiasEntreJornadas() != null ? config.getDiasEntreJornadas() : 7);
        int partidosEliminatoria = config != null && config.getPartidosEliminatoria() != null ? config.getPartidosEliminatoria() : 1;

        List<Equipo> clasificados = clasificacionRepository
                .findByCompeticionIdAndTemporada(competicionId, competicion.getTemporadaActual())
                .stream()
                .filter(c -> c.getPosicion() > 0 && c.getPosicion() <= numEquiposPlayoff)
                .map(Clasificacion::getEquipo)
                .toList();

        if (clasificados.isEmpty()) {
            throw new BadRequestException(
                    "No hay equipos clasificados en la competición. " +
                    "Genera antes el calendario de liga");
        }

        int ronda = rondaInicial != null ? rondaInicial : calcularSiguienteRonda(competicionId);

        return eventoMapper.toDetalleDTOList(
                generarPlayoffSeeded(competicion, clasificados, fechaInicio, ronda, dias, partidosEliminatoria));
    }

    ///  === MÉTODOS DE DOMINIO - TRABAJA CON ENTIDADES - SE PUEDE LLAMAR DESDE OTROS SERVICIOS === ///

    /**
     * Genera y persiste el calendario de la competición trabajando directamente con entidades.
     * <p>
     * Selecciona el {@link GeneradorCalendario} adecuado según el formato de la configuración,
     * genera los eventos e invoca {@code saveAll}. Para el formato {@code EVENTO_UNICO}
     * devuelve una lista vacía sin generar nada.
     * </p>
     *
     * @param competicion competición con configuración cargada
     * @param fechaInicio fecha y hora del primer evento
     * @param diasJornada días entre jornadas consecutivas; no puede ser negativo
     * @return lista de entidades {@link com.compapption.api.entity.Evento} persistidas
     * @throws com.compapption.api.exception.BadRequestException    si no hay configuración, menos de
     *                                                               2 equipos o días negativos
     * @throws com.compapption.api.exception.InternalStateException si no existe generador para el formato
     */
    @Transactional
    public List<Evento> generarCalendario(Competicion competicion, LocalDateTime fechaInicio, Integer diasJornada) {
        ConfiguracionCompeticion config = competicion.getConfiguracion();
        if (config == null) {
            throw new BadRequestException("La competición no tiene configuración");
        }

        List<Equipo> equipos = obtenerEquiposActivos(competicion.getId());
        if (equipos.size() < 2) {
            throw new BadRequestException("Se necesitan al menos 2 equipos para generar el calendario");
        }

        if (diasJornada < 0) {
            throw new BadRequestException("Los días entre jornadas no pueden ser negativos");
        }

        ConfiguracionCompeticion.FormatoCompeticion formato = config.getFormato();

        if (formato == ConfiguracionCompeticion.FormatoCompeticion.EVENTO_UNICO) {
            return List.of();
        }

        GeneradorCalendario generador = generadores.stream()
                .filter(g -> g.soporta(formato))
                .findFirst()
                .orElseThrow(() -> new InternalStateException(
                        "No hay generador disponible para el formato: " + formato));

        List<Evento> eventos = generador.generar(competicion, equipos, fechaInicio, diasJornada,config);

        if (!eventos.isEmpty()) {
            eventoRepository.saveAll(eventos);
        }

        return eventos;
    }

    /**
     * Genera y persiste la fase de playoff seeded con valores por defecto
     * (7 días entre rondas, partido único).
     *
     * @param competicion  competición que recibe el playoff
     * @param clasificados lista de equipos ordenada por clasificación (índice 0 = 1.o puesto)
     * @param fechaInicio  fecha y hora del primer partido
     * @param rondaInicial número de jornada de inicio del bracket
     * @return lista de entidades {@link com.compapption.api.entity.Evento} persistidas
     */
    @Transactional
    public List<Evento> generarPlayoffSeeded(Competicion competicion,
                                             List<Equipo> clasificados,
                                             LocalDateTime fechaInicio,
                                             int rondaInicial) {
        return generarPlayoffSeeded(competicion, clasificados, fechaInicio, rondaInicial, 7, 1);
    }

    /**
     * Genera y persiste la fase de playoff seeded con control total de los parámetros.
     *
     * @param competicion          competición que recibe el playoff
     * @param clasificados         lista de equipos ordenada por clasificación (índice 0 = 1.o puesto)
     * @param fechaInicio          fecha y hora del primer partido
     * @param rondaInicial         número de jornada de inicio del bracket
     * @param diasJornada          días entre rondas del bracket
     * @param partidosEliminatoria número de partidos por eliminatoria (1 = partido único, 2 = ida/vuelta)
     * @return lista de entidades {@link com.compapption.api.entity.Evento} persistidas
     */
    @Transactional
    public List<Evento> generarPlayoffSeeded(Competicion competicion,
                                             List<Equipo> clasificados,
                                             LocalDateTime fechaInicio,
                                             int rondaInicial,
                                             int diasJornada,
                                             int partidosEliminatoria) {
        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, clasificados, fechaInicio, diasJornada, rondaInicial, partidosEliminatoria);

        if (!eventos.isEmpty()) {
            eventoRepository.saveAll(eventos);
        }

        return eventos;
    }

    /// === HELPERS === ///

    private List<Equipo> obtenerEquiposActivos(Long competicionId) {
        return competicionEquipoRepository.findActivosByCompeticionId(competicionId)
                .stream()
                .map(CompeticionEquipo::getEquipo)
                .collect(Collectors.toList());
    }

    private int calcularSiguienteRonda(Long competicionId) {
        Integer maxJornada = eventoRepository.findMaxJornadaByCompeticionId(competicionId);
        return maxJornada != null ? maxJornada + 1 : 1;
    }
}
