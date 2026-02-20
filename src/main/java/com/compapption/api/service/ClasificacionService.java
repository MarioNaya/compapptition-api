package com.compapption.api.service;

import com.compapption.api.dto.clasificacionDTO.ClasificacionDetalleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionSimpleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.ClasificacionMapper;
import com.compapption.api.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClasificacionService {

    private final ClasificacionRepository clasificacionRepository;
    private final CompeticionRepository competicionRepository;
    private final EventoRepository eventoRepository;
    private final EventoEquipoRepository eventoEquipoRepository;
    private final ClasificacionMapper clasificacionMapper;

    @Transactional(readOnly = true)
    public List<ClasificacionDetalleDTO> obtenerClasificacionDetalle(Long competicionId){
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competición", "id", competicionId));
        return clasificacionMapper.toDetalleDTOList(
                clasificacionRepository.findByCompeticionIdAndTemporada(
                        competicionId, competicion.getTemporadaActual())
        );
    }

    @Transactional(readOnly = true)
    public List<ClasificacionSimpleDTO> obtenerClasificacionSimple(Long competicionId){
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competición", "id", competicionId));
        return clasificacionMapper.toSimpleDTOList(
                clasificacionRepository.findByCompeticionIdAndTemporada(
                        competicionId, competicion.getTemporadaActual())
        );
    }

    @Transactional(readOnly = true)
    public ClasificacionDetalleDTO obtenerPorEquipoDetalle(Long competicionId, Long equipoId) {
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));
        return clasificacionMapper.toDetalleDTO(
                clasificacionRepository.findByCompeticionIdAndEquipoIdAndTemporada(
                        competicionId, equipoId, competicion.getTemporadaActual())
                        .orElseThrow(()-> new ResourceNotFoundException("Clasificación", "equipo", equipoId)));
    }

    @Transactional(readOnly = true)
    public ClasificacionSimpleDTO obtenerPorEquipoSimple(Long competicionId, Long equipoId) {
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));
        return clasificacionMapper.toSimpleDTO(
                clasificacionRepository.findByCompeticionIdAndEquipoIdAndTemporada(
                                competicionId, equipoId, competicion.getTemporadaActual())
                        .orElseThrow(()-> new ResourceNotFoundException("Clasificación", "equipo", equipoId)));
    }

    @Transactional(readOnly = true)
    public List<ClasificacionDetalleDTO> obtenerPorTemporadaDetalle(
            Long competicionId, Integer temporada) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return clasificacionMapper.toDetalleDTOList(
                clasificacionRepository.findByCompeticionIdAndTemporada(competicionId, temporada));
    }

    @Transactional(readOnly = true)
    public List<ClasificacionSimpleDTO> obtenerPorTemporadaSimple(
            Long competicionId, Integer temporada) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return clasificacionMapper.toSimpleDTOList(
                clasificacionRepository.findByCompeticionIdAndTemporada(competicionId, temporada));
    }

    @Transactional
    public void inicializarClasificacionEquipo(Competicion competicion, Equipo equipo){
        if (clasificacionRepository.findByCompeticionIdAndEquipoIdAndTemporada (
                competicion.getId(),
                equipo.getId(),
                competicion.getTemporadaActual())
                .isEmpty()){
            Clasificacion clasificacion = Clasificacion.builder()
                    .competicion(competicion)
                    .equipo(equipo)
                    .temporada(competicion.getTemporadaActual())
                    .posicion(0)
                    .puntos(0)
                    .partidosJugados(0)
                    .victorias(0)
                    .empates(0)
                    .derrotas(0)
                    .golesFavor(0)
                    .golesContra(0)
                    .diferenciaGoles(0)
                    .build();

            clasificacionRepository.save(clasificacion);

        }
    }

    @Transactional
    public Map<Long, Clasificacion> resetearClasificacion(Long competicionId, Integer temporada) {
        List<Clasificacion> clasificaciones = clasificacionRepository
                .findByCompeticionIdAndTemporada(competicionId, temporada);
        clasificaciones.forEach(c -> {
            c.setPuntos(0);
            c.setPartidosJugados(0);
            c.setVictorias(0);
            c.setEmpates(0);
            c.setDerrotas(0);
            c.setGolesFavor(0);
            c.setGolesContra(0);
            c.setDiferenciaGoles(0);
        });
        clasificacionRepository.saveAll(clasificaciones);

        return clasificaciones.stream()
                .collect(Collectors.toMap(c -> c.getEquipo().getId(), c -> c));
    }

    @Transactional
    public void calcularClasificacion(Long competicionId){
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));

        ConfiguracionCompeticion config = competicion.getConfiguracion();
        int puntosVictoria = config.getPuntosVictoria();
        int puntosEmpate = config.getPuntosEmpate();
        int puntosDerrota = config.getPuntosDerrota();

        // Resetear la clasificación
        Map<Long, Clasificacion> clasificacionMap = resetearClasificacion(competicionId, competicion.getTemporadaActual());

        // Calcular los resultados de cada evento finalizado
        List<Evento> eventosFinalizados = eventoRepository
                .findFinalizadosByCompeticionIdAndTemporada(
                        competicionId,
                        competicion.getTemporadaActual());

        for (Evento evento : eventosFinalizados) {
            if (evento.getResultadoLocal()==null || evento.getResultadoVisitante()==null) continue;

            List<EventoEquipo> equiposEvento = eventoEquipoRepository.findByEventoId(evento.getId());
            EventoEquipo local = equiposEvento.stream()
                    .filter(EventoEquipo::isEsLocal)
                    .findFirst()
                    .orElse(null);
            EventoEquipo visitante = equiposEvento.stream()
                    .filter(ee -> !ee.isEsLocal())
                    .findFirst()
                    .orElse(null);

            if (local==null || visitante==null) continue;

            Clasificacion clasificacionLocal = clasificacionMap.get(local.getEquipo().getId());
            Clasificacion clasificacionVisitante = clasificacionMap.get(visitante.getEquipo().getId());

            if (clasificacionLocal==null || clasificacionVisitante==null) continue;

            int golesLocal = evento.getResultadoLocal();
            int golesVisitante = evento.getResultadoVisitante();

            // Contar partidos jugados
            clasificacionLocal.setPartidosJugados(clasificacionLocal.getPartidosJugados() + 1);
            clasificacionVisitante.setPartidosJugados(clasificacionVisitante.getPartidosJugados() + 1);

            // Sumar puntos a favor y en contra
            clasificacionLocal.setGolesFavor(clasificacionLocal.getGolesFavor() + golesLocal);
            clasificacionLocal.setGolesContra(clasificacionLocal.getGolesContra() + golesVisitante);
            clasificacionVisitante.setGolesFavor(clasificacionVisitante.getGolesFavor() + golesVisitante);
            clasificacionVisitante.setGolesContra(clasificacionVisitante.getGolesContra() + golesLocal);

            // Contar victorias, empates y derrotas y sumar puntos
            if (golesLocal > golesVisitante) {
                //victoria local
                clasificacionLocal.setVictorias(clasificacionLocal.getVictorias() + 1);
                clasificacionLocal.setPuntos(clasificacionLocal.getPuntos() + puntosVictoria);
                clasificacionVisitante.setDerrotas(clasificacionVisitante.getDerrotas() + 1);
                clasificacionVisitante.setPuntos(clasificacionVisitante.getPuntos() + puntosDerrota);
            } else if (golesLocal < golesVisitante) {
                //victoria visitante
                clasificacionLocal.setDerrotas(clasificacionLocal.getDerrotas() + 1);
                clasificacionLocal.setPuntos(clasificacionLocal.getPuntos() + puntosDerrota);
                clasificacionVisitante.setVictorias(clasificacionVisitante.getVictorias() + 1);
                clasificacionVisitante.setPuntos(clasificacionVisitante.getPuntos() + puntosVictoria);
            } else {
                //empate
                clasificacionLocal.setEmpates(clasificacionLocal.getEmpates() + 1);
                clasificacionLocal.setPuntos(clasificacionLocal.getPuntos() + puntosEmpate);
                clasificacionVisitante.setEmpates(clasificacionVisitante.getEmpates() + 1);
                clasificacionVisitante.setPuntos(clasificacionVisitante.getPuntos() + puntosEmpate);
            }
        }

        // Extraer lista del map
        List<Clasificacion> clasificaciones = new ArrayList<>(clasificacionMap.values());

        // Calcular diferencia de goles
        clasificaciones.forEach(c ->
                c.setDiferenciaGoles(c.getGolesFavor() - c.getGolesContra()));

        // Ordenar equipos por puntos -> diferencia de goles -> goles a favor
        clasificaciones.sort(Comparator
                .comparing(Clasificacion::getPuntos).reversed()
                .thenComparing(Comparator.comparing(Clasificacion::getDiferenciaGoles).reversed())
                .thenComparing(Comparator.comparing(Clasificacion::getGolesFavor).reversed()));

        // Asignar posición equipo
        for (int i = 0; i < clasificaciones.size(); i++) {
            clasificaciones.get(i).setPosicion(i+1);
        }

        // Guardar
        clasificacionRepository.saveAll(clasificaciones);
    }
}
