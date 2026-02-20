package com.compapption.api.service;

import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.dto.eventoDTO.EventoDetalleDTO;
import com.compapption.api.dto.eventoDTO.EventoResultadoDTO;
import com.compapption.api.dto.eventoDTO.EventoSimpleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EstadisticaMapper;
import com.compapption.api.mapper.EventoMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.estadistica.EstadisticaRequest;
import com.compapption.api.request.evento.EventoCreateRequest;
import com.compapption.api.request.evento.EventoUpdateRequest;
import com.compapption.api.request.evento.ResultadoRequest;
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

    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorCompeticionYEquipo(
            Long competicionId,
            Long equipoId){
        if (!competicionRepository.existsById(equipoId)) {
            throw new ResourceNotFoundException("Competicion", "id", equipoId);
        }
        if (!equipoRepository.existsById(equipoId)) {
            throw new ResourceNotFoundException("Equipo", "id", equipoId);
        }
        return eventoMapper.toSimpleDTOList(
                eventoRepository.findByCompeticionIdAndEquipoId(competicionId,equipoId));
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

    @Transactional
    public EventoDetalleDTO crear(Long competicionId, EventoCreateRequest request){

        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));

        Equipo equipoLocal = equipoRepository.findById(request.getEquipoLocalId())
                .orElseThrow(()-> new ResourceNotFoundException("Equipo local", "id", request.getEquipoLocalId()));

        Equipo equipovisitante =equipoRepository.findById(request.getEquipoVisitanteId())
                .orElseThrow(()-> new ResourceNotFoundException("Equipo visitante", "id", request.getEquipoVisitanteId()));

        if (request.getEquipoLocalId().equals(request.getEquipoVisitanteId())){
            throw new BadRequestException("El equipo local y visitante no pueden ser el mismo");
        }

        Evento evento = Evento.builder()
                .competicion(competicion)
                .jornada(request.getJornada())
                .temporada(competicion.getTemporadaActual())
                .fechaHora(request.getFechaHora())
                .lugar(request.getLugar())
                .observaciones(request.getObservaciones())
                .estado(Evento.EstadoEvento.PROGRAMADO)
                .build();

        evento = eventoRepository.save(evento);

        // Crear eventos equipo
        EventoEquipo eventoLocal = EventoEquipo.builder()
                .evento(evento)
                .equipo(equipoLocal)
                .esLocal(true)
                .build();

        EventoEquipo eventoVisitante = EventoEquipo.builder()
                .evento(evento)
                .equipo(equipovisitante)
                .esLocal(true)
                .build();

        evento.getEquipos().add(eventoLocal);
        evento.getEquipos().add(eventoVisitante);

        return eventoMapper.toDetalleDTO(evento);
    }

    // Actualizar datos generales del evento
    @Transactional
    public EventoDetalleDTO actualizar(Long id, EventoUpdateRequest request){

        Evento evento = eventoRepository.findByIdWithEquipos(id)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", id));

        if (request.getJornada() != null) {
            evento.setJornada(request.getJornada());
        }
        if (request.getFechaHora() != null) {
            evento.setFechaHora(request.getFechaHora());
        }
        if (request.getLugar() != null) {
            evento.setLugar(request.getLugar());
        }
        if (request.getEstado() != null) {
            evento.setEstado(request.getEstado());
        }
        if (request.getObservaciones() != null) {
            evento.setObservaciones(request.getObservaciones());
        }

        evento = eventoRepository.save(evento);
        return eventoMapper.toDetalleDTO(evento);
    }

    // Actualiza únicamente el resultado y el estado a finalizado
    @Transactional
    public EventoResultadoDTO registrarResultado(Long id, ResultadoRequest request){

        Evento evento = eventoRepository.findByIdWithEquipos(id)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", id));

        if (evento.getEstado() == Evento.EstadoEvento.FINALIZADO){
            throw new BadRequestException("El evento ya está finalizado");
        }

        evento.setResultadoLocal(request.getResultadoLocal());
        evento.setResultadoVisitante(request.getResultadoVisitante());
        evento.setEstado(Evento.EstadoEvento.FINALIZADO);

        evento = eventoRepository.save(evento);

        // Recalcular clasificación
        clasificacionService.calcularClasificacion(evento.getCompeticion().getId());

        return eventoMapper.toResultadoDTO(evento);
    }

    @Transactional
    public void eliminar(Long id){
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", id));

        if (evento.getEstado() == Evento.EstadoEvento.FINALIZADO){
            throw new BadRequestException("No se puede eliminar un evento finalizado");
        }

        eventoRepository.delete(evento);
    }

    /// === GESTIÓN DE ESTADÍSTICAS === ///

    // Obtener estadísticas jugador
    @Transactional(readOnly = true)
    public List<EstadisticaJugadorDTO> obtenerEstadisticas(Long eventoId){
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        return estadisticaMapper.toDTOList(estadisticaJugadorEventoRepository.findByEventoId(eventoId));
    }

    // Registrar estadísticas en jugador
    @Transactional
    public EstadisticaJugadorDTO registrarEstadistica(Long eventoId, EstadisticaRequest request){
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", eventoId));

        Jugador jugador = jugadorRepository.findById(request.getJugadorId())
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", request.getJugadorId()));

        TipoEstadistica tipoEstadistica = tipoEstadisticaRepository.findById(request.getTipoEstadisticaId())
                .orElseThrow(()-> new ResourceNotFoundException("Tipo estadística", "id", request.getTipoEstadisticaId()));

        // Revisar si la estadística ya existe
        EstadisticaJugadorEvento estadistica = estadisticaJugadorEventoRepository
                .findByEventoIdAndJugadorIdAndTipoEstadisticaId(eventoId, request.getJugadorId(), request.getJugadorId())
                .orElse(EstadisticaJugadorEvento.builder()
                        .evento(evento)
                        .jugador(jugador)
                        .tipoEstadistica(tipoEstadistica)
                        .build());

        estadistica.setValor(request.getValor());
        estadistica = estadisticaJugadorEventoRepository.save(estadistica);

        return estadisticaMapper.toDTO(estadistica);
    }

    // Eliminar estadística
    @Transactional
    public void eliminarEstadistica(Long eventoId, Long estadisticaId){
        EstadisticaJugadorEvento estadistica = estadisticaJugadorEventoRepository.findById(estadisticaId)
                .orElseThrow(()-> new ResourceNotFoundException("Estadistica", "id", estadisticaId));

        if (!estadistica.getEvento().getId().equals(eventoId)) {
            throw new BadRequestException("La estadistica no pertenece a este evento");
        }
        estadisticaJugadorEventoRepository.delete(estadistica);
    }
}

