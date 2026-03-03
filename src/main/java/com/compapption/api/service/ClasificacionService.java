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

/**
 * Servicio que gestiona el cálculo y la consulta de clasificaciones de competiciones.
 * Implementa la lógica de recálculo completo: resetea los acumuladores de todos los
 * equipos, procesa los eventos finalizados de la temporada actual y recalcula puntos,
 * victorias, empates, derrotas, goles y diferencia de goles antes de asignar posiciones.
 * Depende de {@link CompeticionRepository}, {@link EventoRepository} y
 * {@link EventoEquipoRepository} para obtener los datos necesarios.
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class ClasificacionService {

    private final ClasificacionRepository clasificacionRepository;
    private final CompeticionRepository competicionRepository;
    private final EventoRepository eventoRepository;
    private final EventoEquipoRepository eventoEquipoRepository;
    private final ClasificacionMapper clasificacionMapper;

    /**
     * Devuelve la tabla de clasificación completa (formato detalle) de la temporada actual
     * de una competición.
     *
     * @param competicionId identificador de la competición
     * @return lista ordenada de entradas de clasificación con todos los campos estadísticos
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional(readOnly = true)
    public List<ClasificacionDetalleDTO> obtenerClasificacionDetalle(Long competicionId){
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competición", "id", competicionId));
        return clasificacionMapper.toDetalleDTOList(
                clasificacionRepository.findByCompeticionIdAndTemporada(
                        competicionId, competicion.getTemporadaActual())
        );
    }

    /**
     * Devuelve la tabla de clasificación en formato simple (posición, puntos y equipo)
     * de la temporada actual de una competición.
     *
     * @param competicionId identificador de la competición
     * @return lista ordenada de entradas de clasificación en formato resumido
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional(readOnly = true)
    public List<ClasificacionSimpleDTO> obtenerClasificacionSimple(Long competicionId){
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competición", "id", competicionId));
        return clasificacionMapper.toSimpleDTOList(
                clasificacionRepository.findByCompeticionIdAndTemporada(
                        competicionId, competicion.getTemporadaActual())
        );
    }

    /**
     * Obtiene la entrada de clasificación detallada de un equipo concreto en la temporada
     * actual de una competición.
     *
     * @param competicionId identificador de la competición
     * @param equipoId identificador del equipo
     * @return DTO con todos los datos estadísticos del equipo en la clasificación
     * @throws ResourceNotFoundException si la competición o la entrada de clasificación no existen
     */
    @Transactional(readOnly = true)
    public ClasificacionDetalleDTO obtenerPorEquipoDetalle(Long competicionId, Long equipoId) {
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));
        return clasificacionMapper.toDetalleDTO(
                clasificacionRepository.findByCompeticionIdAndEquipoIdAndTemporada(
                        competicionId, equipoId, competicion.getTemporadaActual())
                        .orElseThrow(()-> new ResourceNotFoundException("Clasificación", "equipo", equipoId)));
    }

    /**
     * Obtiene la entrada de clasificación en formato simple de un equipo concreto en la
     * temporada actual de una competición.
     *
     * @param competicionId identificador de la competición
     * @param equipoId identificador del equipo
     * @return DTO simple con la posición y puntos del equipo
     * @throws ResourceNotFoundException si la competición o la entrada de clasificación no existen
     */
    @Transactional(readOnly = true)
    public ClasificacionSimpleDTO obtenerPorEquipoSimple(Long competicionId, Long equipoId) {
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));
        return clasificacionMapper.toSimpleDTO(
                clasificacionRepository.findByCompeticionIdAndEquipoIdAndTemporada(
                                competicionId, equipoId, competicion.getTemporadaActual())
                        .orElseThrow(()-> new ResourceNotFoundException("Clasificación", "equipo", equipoId)));
    }

    /**
     * Devuelve la tabla de clasificación detallada de una temporada histórica concreta.
     *
     * @param competicionId identificador de la competición
     * @param temporada número de temporada a consultar
     * @return lista de entradas de clasificación en formato detalle para esa temporada
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional(readOnly = true)
    public List<ClasificacionDetalleDTO> obtenerPorTemporadaDetalle(
            Long competicionId, Integer temporada) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return clasificacionMapper.toDetalleDTOList(
                clasificacionRepository.findByCompeticionIdAndTemporada(competicionId, temporada));
    }

    /**
     * Devuelve la tabla de clasificación en formato simple de una temporada histórica concreta.
     *
     * @param competicionId identificador de la competición
     * @param temporada número de temporada a consultar
     * @return lista de entradas de clasificación en formato simple para esa temporada
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional(readOnly = true)
    public List<ClasificacionSimpleDTO> obtenerPorTemporadaSimple(
            Long competicionId, Integer temporada) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return clasificacionMapper.toSimpleDTOList(
                clasificacionRepository.findByCompeticionIdAndTemporada(competicionId, temporada));
    }

    /**
     * Crea la entrada de clasificación para un equipo en la temporada actual de la competición,
     * inicializando todos los contadores a cero. Si ya existe una entrada para ese equipo y
     * temporada, no se realiza ninguna acción (idempotente).
     *
     * @param competicion entidad de la competición a la que pertenece el equipo
     * @param equipo entidad del equipo para el que se crea la entrada
     */
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

    /**
     * Pone a cero todos los contadores de clasificación de una competición para una temporada
     * concreta. Devuelve un mapa indexado por id de equipo para uso eficiente durante el
     * recálculo posterior.
     *
     * @param competicionId identificador de la competición
     * @param temporada número de temporada cuyos datos se van a resetear
     * @return mapa de equipoId a entidad Clasificacion con los contadores a cero
     */
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

    /**
     * Recalcula completamente la tabla de clasificación de la temporada actual de una
     * competición. El proceso es: (1) resetear todos los contadores a cero, (2) procesar
     * cada evento finalizado aplicando los puntos de la configuración (victoria, empate,
     * derrota), (3) calcular la diferencia de goles, (4) ordenar por puntos, diferencia de
     * goles y goles a favor, y (5) asignar posiciones. Este método es llamado automáticamente
     * tras registrar el resultado de un evento.
     *
     * @param competicionId identificador de la competición cuya clasificación se recalcula
     * @throws ResourceNotFoundException si la competición no existe
     */
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
