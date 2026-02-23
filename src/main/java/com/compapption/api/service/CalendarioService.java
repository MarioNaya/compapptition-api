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
 * Orquestador del patrón Strategy para generación de calendarios.
 * Delega el algoritmo concreto al GeneradorCalendario que soporta el formato
 * de la competición, elegido en tiempo de ejecución de la lista de beans inyectados.
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
     * Genera el calendario completo de una competición según su formato.
     * Persiste los eventos y devuelve los DTOs.
     */
    @Transactional
    public List<EventoDetalleDTO> generarCalendarioPorIdDetalle(Long competicionId, LocalDateTime fechaInicio, Integer diasJornada) {
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", competicionId));
        return eventoMapper.toDetalleDTOList(generarCalendario(competicion,fechaInicio,diasJornada));
    }

    /**
     * Genera la fase eliminatoria (playoff seeded) para LIGA_PLAYOFF o GRUPOS_PLAYOFF.
     * Toma los mejores N equipos de la clasificación actual.
     * Si rondaInicial es null, se calcula como maxJornada + 1.
     */
    @Transactional
    public List<EventoDetalleDTO> generarPlayoffSeededPorIdDetalle(Long competicionId,
                                                            LocalDateTime fechaInicio,
                                                            Integer rondaInicial) {
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));

        ConfiguracionCompeticion config = competicion.getConfiguracion();
        int numEquiposPlayoff = config != null ? config.getNumEquiposPlayoff() : 8;

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
                generarPlayoffSeeded(competicion, clasificados, fechaInicio, ronda));
    }

    ///  === MÉTODOS DE DOMINIO - TRABAJA CON ENTIDADES - SE PUEDE LLAMAR DESDE OTROS SERVICIOS === ///

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

    @Transactional
    public List<Evento> generarPlayoffSeeded(Competicion competicion,
                                             List<Equipo> clasificados,
                                             LocalDateTime fechaInicio,
                                             int rondaInicial) {
        List<Evento> eventos =
                generadorPlayoff.generarBracket(competicion, clasificados, fechaInicio, null, rondaInicial);

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
