package com.compapption.api.service;

import com.compapption.api.dto.estadisticaDTO.EstadisticaAcumuladaDTO;
import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.entity.EstadisticaJugadorEvento;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EstadisticaMapper;
import com.compapption.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona la consulta y agregación de estadísticas individuales de jugadores.
 * Permite obtener estadísticas filtradas por jugador, evento, temporada o competición, así
 * como calcular acumulados por tipo de estadística y generar rankings dentro de una
 * competición. Las estadísticas brutas (por evento) se registran a través de
 * {@link EventoService}; este servicio se encarga exclusivamente de las lecturas y
 * agregaciones transversales.
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class EstadisticaService {

    private final EstadisticaJugadorEventoRepository estadisticaRepository;
    private final CompeticionRepository competicionRepository;
    private final EventoRepository eventoRepository;
    private final JugadorRepository jugadorRepository;
    private final TipoEstadisticaRepository tipoEstadisticaRepository;
    private final EstadisticaMapper estadisticaMapper;

    /// === CONSULTAS POR JUGADOR, EVENTO, TEMPORADA Y COMPETICIÓN === ///

    /**
     * Devuelve todas las estadísticas registradas para un jugador en cualquier evento y
     * competición.
     *
     * @param jugadorId identificador del jugador
     * @return lista de estadísticas del jugador (sin filtro de competición ni temporada)
     * @throws ResourceNotFoundException si el jugador no existe
     */
    @Transactional(readOnly = true)
    public List<EstadisticaJugadorDTO> obtenerPorJugador(Long jugadorId) {
        if (!jugadorRepository.existsById(jugadorId)) {
            throw new ResourceNotFoundException("Jugador", "id", jugadorId);
        }
        return estadisticaMapper.toDTOList(estadisticaRepository.findByJugadorId(jugadorId));
    }

    /**
     * Devuelve las estadísticas de un jugador en un evento concreto.
     *
     * @param eventoId identificador del evento
     * @param jugadorId identificador del jugador
     * @return lista de estadísticas del jugador en ese evento (un registro por tipo de estadística)
     * @throws ResourceNotFoundException si el jugador o el evento no existen
     */
    @Transactional(readOnly = true)
    public List<EstadisticaJugadorDTO> obtenerPorJugadorEnEvento(Long eventoId, Long jugadorId) {
        if (!jugadorRepository.existsById(jugadorId)) {
            throw new ResourceNotFoundException("Jugador", "id", jugadorId);
        }
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        return estadisticaMapper.toDTOList(
                estadisticaRepository.findByEventoIdAndJugadorId(eventoId, jugadorId));
    }

    /**
     * Devuelve todas las estadísticas de un jugador en una temporada concreta, con
     * independencia de la competición.
     *
     * @param jugadorId identificador del jugador
     * @param temporada número de temporada a filtrar
     * @return lista de estadísticas del jugador en esa temporada
     * @throws ResourceNotFoundException si el jugador no existe
     */
    @Transactional(readOnly = true)
    public List<EstadisticaJugadorDTO> obtenerPorJugadorEnTemporada(Long jugadorId, Integer temporada) {
        if (!jugadorRepository.existsById(jugadorId)) {
            throw new ResourceNotFoundException("Jugador", "id", jugadorId);
        }
        return estadisticaMapper.toDTOList(
                estadisticaRepository.findByJugadorIdAndTemporada(jugadorId, temporada));
    }

    /**
     * Devuelve todas las estadísticas de un jugador dentro de una competición, abarcando
     * todos los eventos y temporadas de esa competición.
     *
     * @param competicionId identificador de la competición
     * @param jugadorId identificador del jugador
     * @return lista de estadísticas del jugador en esa competición
     * @throws ResourceNotFoundException si la competición o el jugador no existen
     */
    @Transactional(readOnly = true)
    public List<EstadisticaJugadorDTO> obtenerPorJugadorEnCompeticion(Long competicionId, Long jugadorId) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        if (!jugadorRepository.existsById(jugadorId)) {
            throw new ResourceNotFoundException("Jugador", "id", jugadorId);
        }
        return estadisticaMapper.toDTOList(
                estadisticaRepository.findByCompeticionIdAndJugadorId(competicionId, jugadorId));
    }

    /// === ESTADÍSTICA ACUMULADA POR TIPO ESTADÍSTICA === ///

    /**
     * Calcula el acumulado de estadísticas de un jugador en una competición, agrupado por
     * tipo de estadística. Suma los valores de todos los eventos en los que ha participado
     * el jugador dentro de esa competición y devuelve un registro por tipo (ej. goles totales,
     * asistencias totales, etc.), ordenado por id de tipo de estadística.
     *
     * @param competicionId identificador de la competición
     * @param jugadorId identificador del jugador
     * @return lista de acumulados por tipo de estadística, ordenada por tipoEstadisticaId
     * @throws ResourceNotFoundException si la competición o el jugador no existen
     */
    @Transactional(readOnly = true)
    public List<EstadisticaAcumuladaDTO> obtenerAcumuladoEnCompeticion(Long competicionId, Long jugadorId) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        if (!jugadorRepository.existsById(jugadorId)) {
            throw new ResourceNotFoundException("Jugador", "id", jugadorId);
        }

        return estadisticaRepository.findByCompeticionIdAndJugadorId(competicionId, jugadorId)
                .stream()
                .collect(Collectors.groupingBy(e -> e.getTipoEstadistica().getId()))
                .values().stream()
                .map(this::buildAcumuladaDTO)
                .sorted(Comparator.comparing(EstadisticaAcumuladaDTO::getTipoEstadisticaId))
                .collect(Collectors.toList());
    }

    /// === RANKING ESTADÍSTICAS POR TIPO Y COMPETICIÓN === ///

    /**
     * Genera el ranking de todos los jugadores de una competición para un tipo de
     * estadística concreto (ej. tabla de goleadores, tabla de asistentes). Agrupa por
     * jugador, suma el total acumulado y devuelve la lista ordenada de mayor a menor valor.
     *
     * @param competicionId identificador de la competición
     * @param tipoEstadisticaId identificador del tipo de estadística por el que se rankea
     * @return lista de jugadores con su acumulado, ordenada de mayor a menor total
     * @throws ResourceNotFoundException si la competición o el tipo de estadística no existen
     */
    @Transactional(readOnly = true)
    public List<EstadisticaAcumuladaDTO> obtenerRankingEnCompeticion(Long competicionId, Long tipoEstadisticaId) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        if (!tipoEstadisticaRepository.existsById(tipoEstadisticaId)) {
            throw new ResourceNotFoundException("Tipo estadística", "id", tipoEstadisticaId);
        }

        return estadisticaRepository.findByCompeticionIdAndTipoEstadisticaId(competicionId, tipoEstadisticaId)
                .stream()
                .collect(Collectors.groupingBy(e -> e.getJugador().getId()))
                .values().stream()
                .map(this::buildAcumuladaDTO)
                .sorted(Comparator.comparing(EstadisticaAcumuladaDTO::getTotal).reversed())
                .collect(Collectors.toList());
    }

    /// === HELPER CONSTRUCTOR DE ESTADÍSTICA ACUMULADA === ///

    /**
     * Construye un DTO de estadística acumulada a partir de una lista de registros de un
     * mismo jugador y tipo de estadística. Suma todos los valores para obtener el total y
     * compone el nombre completo del jugador.
     *
     * @param estadisticasJugador lista de registros individuales del mismo jugador y tipo
     *                            (debe contener al menos un elemento)
     * @return DTO con el total acumulado, datos del jugador y del tipo de estadística
     */
    private EstadisticaAcumuladaDTO buildAcumuladaDTO(List<EstadisticaJugadorEvento> estadisticasJugador) {
        EstadisticaJugadorEvento primera = estadisticasJugador.get(0);

        BigDecimal total = estadisticasJugador.stream()
                .map(EstadisticaJugadorEvento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String jugadorNombre = primera.getJugador().getNombre()
                + (primera.getJugador().getApellidos() != null
                ? " " + primera.getJugador().getApellidos() : "");

        return EstadisticaAcumuladaDTO.builder()
                .jugadorId(primera.getJugador().getId())
                .jugadorNombre(jugadorNombre)
                .tipoEstadisticaId(primera.getTipoEstadistica().getId())
                .tipoEstadisticaNombre(primera.getTipoEstadistica().getNombre())
                .total(total)
                .build();
    }
}
