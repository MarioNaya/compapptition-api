package com.compapption.api.service;

import com.compapption.api.dto.eventoDTO.EventoDetalleDTO;
import com.compapption.api.dto.eventoDTO.EventoSimpleDTO;
import com.compapption.api.entity.Evento;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EstadisticaMapper;
import com.compapption.api.mapper.EventoMapper;
import com.compapption.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CompeticionRepository competicionRepository;
    private final EquipoRepository equipoRepository;
    private final EventoEquipoRepository eventoEquipoRepository;
    private final EstadisticaJugadorEventoRepository estadisticaJugadorEventoRepository;
    private final TipoEstadisticaRepository tipoEstadisticaRepository;
    private final JugadorRepository jugadorRepository;
    private final EventoMapper eventoMapper;
    private final EstadisticaMapper estadisticaMapper;
    private final ClasificacionService clasificacionService;

    /// === CONSULTAS EVENTOS === ///

    // Por Id

    @Transactional(readOnly = true)
    public EventoSimpleDTO obtenerPorIdSimple(Long id){
        Evento evento = eventoRepository.findByIdWithEquipos(id)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", id));
        return eventoMapper.toSimpleDTO(evento);
    }

    @Transactional(readOnly = true)
    public EventoDetalleDTO obtenerPorIdDetalle(Long id){
        Evento evento = eventoRepository.findByIdWithEquipos(id)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", id));
        return eventoMapper.toDetalleDTO(evento);
    }

    // Por competición

    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorCompeticionSimple(Long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return eventoMapper.toSimpleDTOList(eventoRepository.findByCompeticionIdOrdered(competicionId));
    }

    @Transactional(readOnly = true)
    public List<EventoDetalleDTO> obtenerPorCompeticionDetalle(Long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return eventoMapper.toDetalleDTOList(eventoRepository.findByCompeticionIdOrdered(competicionId));
    }

    // Por jornada

    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorJornadaSimple(Long competicionId, Integer jornada){
        return eventoMapper.toSimpleDTOList(
                eventoRepository.findByCompeticionIdAndJornada(competicionId, jornada));
    }

    @Transactional(readOnly = true)
    public List<EventoDetalleDTO> obtenerPorJornadaDetalle(Long competicionId, Integer jornada){
        return eventoMapper.toDetalleDTOList(
                eventoRepository.findByCompeticionIdAndJornada(competicionId, jornada));
    }

    // Por equipo

    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorEquipoSimple(Long equipoId){
        if (!equipoRepository.existsById(equipoId)){
            throw new ResourceNotFoundException("Equipo", "id", equipoId);
        }
        return eventoMapper.toSimpleDTOList(
                eventoRepository.findByEquipoId(equipoId));
    }

    @Transactional(readOnly = true)
    public List<EventoDetalleDTO> obtenerPorEquipoDetalle(Long equipoId){
        if (!equipoRepository.existsById(equipoId)){
            throw new ResourceNotFoundException("Equipo", "id", equipoId);
        }
        return eventoMapper.toDetalleDTOList(
                eventoRepository.findByEquipoId(equipoId));
    }

    // Por fecha

    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorRangoFechasSimple(
            Long competicionId,
            LocalDateTime inicio,
            LocalDateTime fin){
        return eventoMapper.toSimpleDTOList(
                eventoRepository.findByCompeticionIdAndFechaHoraBetween(
                        competicionId,
                        inicio,
                        fin));
    }

    @Transactional(readOnly = true)
    public List<EventoDetalleDTO> obtenerPorRangoFechasDetalle(
            Long competicionId,
            LocalDateTime inicio,
            LocalDateTime fin){
        return eventoMapper.toDetalleDTOList(
                eventoRepository.findByCompeticionIdAndFechaHoraBetween(
                        competicionId,
                        inicio,
                        fin));
    }

    /// === CREAR, ACTUALIZAR Y ELIMINAR EVENTO === ///


}

