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
import com.compapption.api.service.log.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio que gestiona el ciclo de vida completo de los eventos (partidos) de una
 * competición. Cubre la creación y actualización de eventos, el registro de resultados
 * con recálculo automático de clasificación, el avance automático del bracket en
 * competiciones de tipo playoff (partido único, serie ida/vuelta y best-of-N) y la
 * gestión de estadísticas individuales de jugador por evento. Depende de
 * {@link ClasificacionService} para actualizar la tabla tras cada resultado y de
 * {@link LogService} para la auditoría de operaciones.
 *
 * @author Mario
 */
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
    private final LogService logService;

    /// === CONSULTAS EVENTOS === ///

    // Por Id

    /**
     * Obtiene el resumen simple de un evento por su identificador.
     *
     * @param id identificador del evento
     * @return DTO simple con los campos mínimos del evento (equipos, marcador, estado)
     * @throws ResourceNotFoundException si no existe ningún evento con ese id
     */
    @Transactional(readOnly = true)
    public EventoSimpleDTO obtenerPorIdSimple(Long id){
        Evento evento = eventoRepository.findByIdWithEquipos(id)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", id));
        return eventoMapper.toSimpleDTO(evento);
    }

    /**
     * Obtiene el detalle completo de un evento por su identificador.
     *
     * @param id identificador del evento
     * @return DTO con todos los campos del evento, incluidas estadísticas y equipos participantes
     * @throws ResourceNotFoundException si no existe ningún evento con ese id
     */
    @Transactional(readOnly = true)
    public EventoDetalleDTO obtenerPorIdDetalle(Long id){
        Evento evento = eventoRepository.findByIdWithEquipos(id)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", id));
        return eventoMapper.toDetalleDTO(evento);
    }

    // Por competición

    /**
     * Lista todos los eventos de una competición en formato simple, ordenados por jornada
     * y fecha.
     *
     * @param competicionId identificador de la competición
     * @return lista de eventos en formato simple
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorCompeticionSimple(Long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return eventoMapper.toSimpleDTOList(eventoRepository.findByCompeticionIdOrdered(competicionId));
    }

    /**
     * Lista todos los eventos de una competición en formato detalle, ordenados por jornada
     * y fecha.
     *
     * @param competicionId identificador de la competición
     * @return lista de eventos en formato detalle
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional(readOnly = true)
    public List<EventoDetalleDTO> obtenerPorCompeticionDetalle(Long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return eventoMapper.toDetalleDTOList(eventoRepository.findByCompeticionIdOrdered(competicionId));
    }

    // Por jornada

    /**
     * Lista los eventos de una jornada concreta de una competición en formato simple.
     *
     * @param competicionId identificador de la competición
     * @param jornada número de jornada a consultar
     * @return lista de eventos de esa jornada en formato simple
     */
    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorJornadaSimple(Long competicionId, Integer jornada){
        return eventoMapper.toSimpleDTOList(
                eventoRepository.findByCompeticionIdAndJornada(competicionId, jornada));
    }

    /**
     * Lista los eventos de una jornada concreta de una competición en formato detalle.
     *
     * @param competicionId identificador de la competición
     * @param jornada número de jornada a consultar
     * @return lista de eventos de esa jornada en formato detalle
     */
    @Transactional(readOnly = true)
    public List<EventoDetalleDTO> obtenerPorJornadaDetalle(Long competicionId, Integer jornada){
        return eventoMapper.toDetalleDTOList(
                eventoRepository.findByCompeticionIdAndJornada(competicionId, jornada));
    }

    // Por equipo

    /**
     * Lista todos los eventos en los que ha participado un equipo, en formato simple.
     *
     * @param equipoId identificador del equipo
     * @return lista de eventos del equipo en formato simple
     * @throws ResourceNotFoundException si el equipo no existe
     */
    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorEquipoSimple(Long equipoId){
        if (!equipoRepository.existsById(equipoId)){
            throw new ResourceNotFoundException("Equipo", "id", equipoId);
        }
        return eventoMapper.toSimpleDTOList(
                eventoRepository.findByEquipoId(equipoId));
    }

    /**
     * Lista todos los eventos en los que ha participado un equipo, en formato detalle.
     *
     * @param equipoId identificador del equipo
     * @return lista de eventos del equipo en formato detalle
     * @throws ResourceNotFoundException si el equipo no existe
     */
    @Transactional(readOnly = true)
    public List<EventoDetalleDTO> obtenerPorEquipoDetalle(Long equipoId){
        if (!equipoRepository.existsById(equipoId)){
            throw new ResourceNotFoundException("Equipo", "id", equipoId);
        }
        return eventoMapper.toDetalleDTOList(
                eventoRepository.findByEquipoId(equipoId));
    }

    /**
     * Lista los eventos de un equipo dentro de una competición concreta, en formato simple.
     *
     * @param competicionId identificador de la competición
     * @param equipoId identificador del equipo
     * @return lista de eventos del equipo en esa competición en formato simple
     * @throws ResourceNotFoundException si la competición o el equipo no existen
     */
    @Transactional(readOnly = true)
    public List<EventoSimpleDTO> obtenerPorCompeticionYEquipo(
            Long competicionId,
            Long equipoId){
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competicion", "id", competicionId);
        }
        if (!equipoRepository.existsById(equipoId)) {
            throw new ResourceNotFoundException("Equipo", "id", equipoId);
        }
        return eventoMapper.toSimpleDTOList(
                eventoRepository.findByCompeticionIdAndEquipoId(competicionId,equipoId));
    }

    // Por fecha

    /**
     * Lista los eventos de una competición cuya fecha y hora se encuentran dentro de un
     * rango determinado, en formato simple.
     *
     * @param competicionId identificador de la competición
     * @param inicio inicio del rango de fechas (inclusivo)
     * @param fin fin del rango de fechas (inclusivo)
     * @return lista de eventos dentro del rango en formato simple
     */
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

    /**
     * Lista los eventos de una competición cuya fecha y hora se encuentran dentro de un
     * rango determinado, en formato detalle.
     *
     * @param competicionId identificador de la competición
     * @param inicio inicio del rango de fechas (inclusivo)
     * @param fin fin del rango de fechas (inclusivo)
     * @return lista de eventos dentro del rango en formato detalle
     */
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

    /**
     * Crea un nuevo evento (partido) en una competición, asignando los equipos local y
     * visitante. El evento se crea en estado PROGRAMADO. Regla de negocio: el equipo local
     * y el visitante no pueden ser el mismo equipo.
     *
     * @param competicionId identificador de la competición a la que pertenece el evento
     * @param request datos del nuevo evento: equipos, jornada, fecha, lugar y observaciones
     * @return DTO con el detalle del evento creado, incluyendo los equipos participantes
     * @throws ResourceNotFoundException si la competición o alguno de los equipos no existen
     * @throws BadRequestException si el equipo local y el visitante son el mismo
     */
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
                .esLocal(false)
                .build();

        eventoEquipoRepository.save(eventoLocal);
        eventoEquipoRepository.save(eventoVisitante);
        evento.getEquipos().add(eventoLocal);
        evento.getEquipos().add(eventoVisitante);

        logService.registrar("Evento", evento.getId(), LogModificacion.AccionLog.CREAR, null, null, competicionId);
        return eventoMapper.toDetalleDTO(evento);
    }

    /**
     * Actualiza los datos generales de un evento (jornada, fecha, lugar, estado u
     * observaciones). Solo se modifican los campos no nulos del request (actualización
     * parcial). No modifica el resultado: para ello usar {@link #registrarResultado}.
     *
     * @param id identificador del evento a actualizar
     * @param request campos a modificar; los nulos se ignoran
     * @return DTO con el detalle actualizado del evento
     * @throws ResourceNotFoundException si el evento no existe
     */
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
        logService.registrar("Evento", evento.getId(), LogModificacion.AccionLog.EDITAR, null, null, evento.getCompeticion().getId());
        return eventoMapper.toDetalleDTO(evento);
    }

    /**
     * Registra el resultado de un evento y lo marca como FINALIZADO. Tras guardar el
     * resultado, recalcula automáticamente la clasificación de la competición y, si la
     * competición tiene bracket de playoff, ejecuta el avance automático del ganador a la
     * siguiente ronda. Regla de negocio: no se puede registrar resultado en un evento ya
     * finalizado.
     *
     * @param id identificador del evento
     * @param request marcadores final del equipo local y del visitante
     * @return DTO con el resultado registrado y el estado final del evento
     * @throws ResourceNotFoundException si el evento no existe
     * @throws BadRequestException si el evento ya está en estado FINALIZADO
     */
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

        // Avance automático de bracket playoff
        procesarAvancePlayoff(evento);

        logService.registrar("Evento", evento.getId(), LogModificacion.AccionLog.EDITAR, null, null, evento.getCompeticion().getId());
        return eventoMapper.toResultadoDTO(evento);
    }

    /// === LÓGICA DE AVANCE AUTOMÁTICO EN BRACKET PLAYOFF === ///

    /**
     * Tras finalizar un evento, comprueba si pertenece a un bracket de playoff
     * y, si la eliminatoria está resuelta, rellena el equipo ganador en el evento
     * de la siguiente ronda.
     */
    private void procesarAvancePlayoff(Evento eventoFinalizado) {
        boolean esSerie = eventoFinalizado.getNumeroPartido() != null;

        Evento decisivo;
        Optional<Equipo> ganadorOpt;

        if (!esSerie) {
            // Partido único: este evento es el decisivo
            decisivo = eventoFinalizado;
            ganadorOpt = determinarGanadorPartidoUnico(eventoFinalizado);
        } else {
            // Serie: buscar todos los partidos de la eliminatoria
            List<Evento> serie = encontrarSerie(eventoFinalizado);
            int maxPartidos = serie.stream()
                    .mapToInt(e -> e.getNumeroPartido() != null ? e.getNumeroPartido() : 1)
                    .max().orElse(1);

            ganadorOpt = determinarGanador(serie, maxPartidos);
            if (ganadorOpt.isEmpty()) return;

            // El evento "decisivo" es el último posible de la serie (el que referencia la siguiente ronda)
            decisivo = serie.stream()
                    .max(Comparator.comparingInt(e -> e.getNumeroPartido() != null ? e.getNumeroPartido() : 0))
                    .orElse(eventoFinalizado);
        }

        if (ganadorOpt.isEmpty()) return;
        Equipo ganador = ganadorOpt.get();

        // Buscar eventos de siguiente ronda que referencian este partido como decisivo
        List<Evento> siguientes = eventoRepository.findByPartidoAnteriorId(decisivo.getId());
        if (siguientes.isEmpty()) return;

        for (Evento siguiente : siguientes) {
            // Evitar duplicados
            boolean yaExiste = eventoEquipoRepository.findByEventoId(siguiente.getId())
                    .stream().anyMatch(ee -> ee.getEquipo().getId().equals(ganador.getId()));
            if (yaExiste) continue;

            // El rol depende de qué campo anterior apunta al decisivo:
            // anteriorLocal → ganador juega como local; anteriorVisitante → como visitante
            Long anteriorLocalId = siguiente.getPartidoAnteriorLocal() != null
                    ? siguiente.getPartidoAnteriorLocal().getId() : null;
            boolean esLocal = decisivo.getId().equals(anteriorLocalId);

            eventoEquipoRepository.save(EventoEquipo.builder()
                    .evento(siguiente)
                    .equipo(ganador)
                    .esLocal(esLocal)
                    .build());
        }
    }

    /**
     * Encuentra todos los eventos de la misma eliminatoria (serie) que el evento dado.
     */
    private List<Evento> encontrarSerie(Evento evento) {
        // Ronda 2+: comparten anteriorLocal y anteriorVisitante
        if (evento.getPartidoAnteriorLocal() != null) {
            return eventoRepository.findSerieByAnteriores(
                    evento.getPartidoAnteriorLocal().getId(),
                    evento.getPartidoAnteriorVisitante().getId());
        }

        // Ronda 1: buscar por los mismos dos equipos
        Evento conEquipos = eventoRepository.findByIdWithEquipos(evento.getId())
                .orElse(evento);
        Long localId = conEquipos.getEquipos().stream()
                .filter(EventoEquipo::isEsLocal)
                .map(ee -> ee.getEquipo().getId())
                .findFirst().orElse(null);
        Long visitanteId = conEquipos.getEquipos().stream()
                .filter(ee -> !ee.isEsLocal())
                .map(ee -> ee.getEquipo().getId())
                .findFirst().orElse(null);

        if (localId == null || visitanteId == null) return List.of(evento);

        return eventoRepository.findSerieRonda1ByEquipos(
                evento.getCompeticion().getId(), localId, visitanteId);
    }

    /**
     * Evalúa si una serie está resuelta y devuelve el ganador.
     * @param maxPartidos número máximo de partidos posibles (1, 2, 3, 5 o 7)
     */
    private Optional<Equipo> determinarGanador(List<Evento> serie, int maxPartidos) {
        List<Evento> finalizados = serie.stream()
                .filter(e -> e.getEstado() == Evento.EstadoEvento.FINALIZADO)
                .toList();

        if (maxPartidos == 1) {
            if (finalizados.isEmpty()) return Optional.empty();
            return determinarGanadorPartidoUnico(
                    eventoRepository.findByIdWithEquipos(finalizados.get(0).getId())
                            .orElse(finalizados.get(0)));
        }

        if (maxPartidos == 2) {
            // Ida/vuelta: decidida cuando ambos juegos están finalizados
            if (finalizados.size() < 2) return Optional.empty();
            return determinarGanadorAgregado(finalizados);
        }

        // Best-of-N
        int threshold = (maxPartidos + 1) / 2;
        return determinarGanadorBestOf(finalizados, threshold);
    }

    /**
     * Determina el ganador de un partido único comparando los marcadores. Si el resultado
     * está empatado o no se han registrado aún los goles, devuelve vacío y no se produce
     * avance automático.
     *
     * @param e evento finalizado con sus equipos cargados
     * @return el equipo ganador, o vacío si hay empate o resultado nulo
     */
    private Optional<Equipo> determinarGanadorPartidoUnico(Evento e) {
        if (e.getResultadoLocal() == null || e.getResultadoVisitante() == null) return Optional.empty();
        if (e.getResultadoLocal() > e.getResultadoVisitante()) {
            return e.getEquipos().stream()
                    .filter(EventoEquipo::isEsLocal)
                    .map(EventoEquipo::getEquipo)
                    .findFirst();
        }
        if (e.getResultadoLocal() < e.getResultadoVisitante()) {
            return e.getEquipos().stream()
                    .filter(ee -> !ee.isEsLocal())
                    .map(EventoEquipo::getEquipo)
                    .findFirst();
        }
        return Optional.empty(); // Empate → sin avance automático
    }

    /**
     * Determina el ganador de una eliminatoria de ida y vuelta sumando los goles totales
     * de ambos partidos (marcador agregado). Si el agregado está empatado, devuelve vacío
     * y no se produce avance automático.
     *
     * @param finalizados lista con exactamente los dos partidos finalizados de la eliminatoria
     * @return el equipo con mayor marcador agregado, o vacío si hay empate
     */
    private Optional<Equipo> determinarGanadorAgregado(List<Evento> finalizados) {
        Map<Long, Integer> totalGoles = new HashMap<>();
        for (Evento e : finalizados) {
            Evento conEq = eventoRepository.findByIdWithEquipos(e.getId()).orElse(e);
            for (EventoEquipo ee : conEq.getEquipos()) {
                int goles = ee.isEsLocal()
                        ? (conEq.getResultadoLocal() != null ? conEq.getResultadoLocal() : 0)
                        : (conEq.getResultadoVisitante() != null ? conEq.getResultadoVisitante() : 0);
                totalGoles.merge(ee.getEquipo().getId(), goles, Integer::sum);
            }
        }
        if (totalGoles.size() != 2) return Optional.empty();

        List<Map.Entry<Long, Integer>> sorted = totalGoles.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .toList();
        if (sorted.get(0).getValue().equals(sorted.get(1).getValue())) return Optional.empty();

        Long ganadorId = sorted.get(0).getKey();
        return finalizados.stream()
                .flatMap(e -> e.getEquipos().stream())
                .filter(ee -> ee.getEquipo().getId().equals(ganadorId))
                .map(EventoEquipo::getEquipo)
                .findFirst();
    }

    /**
     * Determina el ganador de una serie best-of-N contando victorias de cada equipo. Un
     * equipo gana la serie en cuanto alcanza el umbral de victorias necesarias (threshold).
     * Los empates en partidos individuales no se contabilizan como victoria para ningún equipo.
     *
     * @param finalizados lista de partidos ya finalizados de la serie
     * @param threshold número de victorias necesarias para ganar la serie (ej. 2 en un best-of-3)
     * @return el equipo que ha alcanzado el umbral de victorias, o vacío si ninguno lo ha hecho aún
     */
    private Optional<Equipo> determinarGanadorBestOf(List<Evento> finalizados, int threshold) {
        Map<Long, Integer> victorias = new HashMap<>();
        for (Evento e : finalizados) {
            if (e.getResultadoLocal() == null || e.getResultadoVisitante() == null) continue;
            Evento conEq = eventoRepository.findByIdWithEquipos(e.getId()).orElse(e);
            Optional<EventoEquipo> ganadorEe;
            if (e.getResultadoLocal() > e.getResultadoVisitante()) {
                ganadorEe = conEq.getEquipos().stream().filter(EventoEquipo::isEsLocal).findFirst();
            } else if (e.getResultadoLocal() < e.getResultadoVisitante()) {
                ganadorEe = conEq.getEquipos().stream().filter(ee -> !ee.isEsLocal()).findFirst();
            } else {
                continue; // Empate no cuenta como victoria en best-of
            }
            ganadorEe.ifPresent(ee -> victorias.merge(ee.getEquipo().getId(), 1, Integer::sum));
        }

        for (Map.Entry<Long, Integer> entry : victorias.entrySet()) {
            if (entry.getValue() >= threshold) {
                Long ganadorId = entry.getKey();
                return finalizados.stream()
                        .flatMap(e -> e.getEquipos().stream())
                        .filter(ee -> ee.getEquipo().getId().equals(ganadorId))
                        .map(EventoEquipo::getEquipo)
                        .findFirst();
            }
        }
        return Optional.empty();
    }

    /**
     * Elimina permanentemente un evento. Regla de negocio: no se puede eliminar un evento
     * que ya está en estado FINALIZADO para preservar la integridad histórica de resultados
     * y clasificaciones.
     *
     * @param id identificador del evento a eliminar
     * @throws ResourceNotFoundException si el evento no existe
     * @throws BadRequestException si el evento ya está finalizado
     */
    @Transactional
    public void eliminar(Long id){
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Evento", "id", id));

        if (evento.getEstado() == Evento.EstadoEvento.FINALIZADO){
            throw new BadRequestException("No se puede eliminar un evento finalizado");
        }

        logService.registrar("Evento", id, LogModificacion.AccionLog.ELIMINAR, null, null, evento.getCompeticion().getId());
        eventoRepository.delete(evento);
    }

    /// === GESTIÓN DE ESTADÍSTICAS === ///

    // Obtener estadísticas jugador
    /**
     * Devuelve todas las estadísticas individuales de jugadores registradas para un evento.
     *
     * @param eventoId identificador del evento
     * @return lista de estadísticas de jugadores en ese evento
     * @throws ResourceNotFoundException si el evento no existe
     */
    @Transactional(readOnly = true)
    public List<EstadisticaJugadorDTO> obtenerEstadisticas(Long eventoId){
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        return estadisticaMapper.toDTOList(estadisticaJugadorEventoRepository.findByEventoId(eventoId));
    }

    // Registrar estadísticas en jugador
    /**
     * Registra o actualiza una estadística individual de un jugador en un evento. Si ya
     * existe un registro para la combinación evento + jugador + tipo de estadística, se
     * sobreescribe el valor; en caso contrario se crea uno nuevo (upsert).
     *
     * @param eventoId identificador del evento
     * @param request datos de la estadística: jugador, tipo y valor numérico
     * @return DTO con la estadística creada o actualizada
     * @throws ResourceNotFoundException si el evento, el jugador o el tipo de estadística no existen
     */
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
                .findByEventoIdAndJugadorIdAndTipoEstadisticaId(eventoId, request.getJugadorId(), request.getTipoEstadisticaId())
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
    /**
     * Elimina una estadística individual de un jugador en un evento. Verifica que la
     * estadística pertenezca al evento indicado antes de borrarla para evitar borrados
     * cruzados entre eventos.
     *
     * @param eventoId identificador del evento al que debe pertenecer la estadística
     * @param estadisticaId identificador de la estadística a eliminar
     * @throws ResourceNotFoundException si la estadística no existe
     * @throws BadRequestException si la estadística no pertenece al evento indicado
     */
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

