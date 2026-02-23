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

    @Transactional(readOnly = true)
    public List<EstadisticaJugadorDTO> obtenerPorJugador(Long jugadorId) {
        if (!jugadorRepository.existsById(jugadorId)) {
            throw new ResourceNotFoundException("Jugador", "id", jugadorId);
        }
        return estadisticaMapper.toDTOList(estadisticaRepository.findByJugadorId(jugadorId));
    }

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

    @Transactional(readOnly = true)
    public List<EstadisticaJugadorDTO> obtenerPorJugadorEnTemporada(Long jugadorId, Integer temporada) {
        if (!jugadorRepository.existsById(jugadorId)) {
            throw new ResourceNotFoundException("Jugador", "id", jugadorId);
        }
        return estadisticaMapper.toDTOList(
                estadisticaRepository.findByJugadorIdAndTemporada(jugadorId, temporada));
    }

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
