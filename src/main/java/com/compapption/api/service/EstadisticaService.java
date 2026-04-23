package com.compapption.api.service;

import com.compapption.api.dto.estadisticaDTO.EstadisticaAcumuladaDTO;
import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.entity.EstadisticaJugadorEvento;
import com.compapption.api.entity.Evento;
import com.compapption.api.entity.Jugador;
import com.compapption.api.entity.LogModificacion;
import com.compapption.api.entity.Rol;
import com.compapption.api.entity.TipoEstadistica;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.mapper.EstadisticaMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.estadistica.EstadisticaCreateRequest;
import com.compapption.api.service.log.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final EventoEquipoRepository eventoEquipoRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;
    private final UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    private final EquipoManagerRepository equipoManagerRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadisticaMapper estadisticaMapper;
    private final LogService logService;

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

    /// === REGISTRO MANUAL DE ESTADÍSTICA (POST /estadisticas) === ///

    /**
     * Registra manualmente una estadística de un jugador en un evento, con validación
     * completa de integridad de negocio, permisos y coherencia del valor con el tipo.
     * <p>
     * Comprobaciones realizadas:
     * <ol>
     *   <li>Existencia del evento, jugador y tipo de estadística.</li>
     *   <li>El jugador debe estar inscrito y activo en alguno de los equipos que participan en el evento.</li>
     *   <li>El tipo de estadística debe pertenecer al deporte de la competición del evento.</li>
     *   <li>El usuario debe tener permiso: {@code ADMIN_SISTEMA}, {@code ADMIN_COMPETICION}
     *       o {@code ARBITRO} en la competición, o {@code MANAGER_EQUIPO}
     *       del equipo del jugador en esa competición (rol {@code ANOTADOR} no existe en el sistema).</li>
     *   <li>El valor debe ser coherente con el {@link TipoEstadistica.TipoValor}:
     *       ENTERO (sin decimales), BOOLEANO (0 o 1), DECIMAL (≥ 0), TIEMPO (≥ 0).</li>
     * </ol>
     * Si ya existe un registro para (evento, jugador, tipo) se actualiza (upsert) y se
     * registra un log {@code EDITAR}; en otro caso se crea y se registra log {@code CREAR}.
     *
     * @param request   datos de la estadística a registrar (evento, jugador, tipo, valor)
     * @param usuarioId identificador del usuario que realiza la acción (extraído del JWT)
     * @return DTO con la estadística creada o actualizada
     * @throws ResourceNotFoundException si evento, jugador o tipo de estadística no existen
     * @throws BadRequestException       si el jugador no participa en el evento, el tipo no pertenece
     *                                   al deporte de la competición, o el valor no es coherente con el tipo
     * @throws UnauthorizedException     si el usuario no tiene permiso para registrar estadísticas en esta competición
     */
    @Transactional
    public EstadisticaJugadorDTO registrarEstadistica(EstadisticaCreateRequest request, Long usuarioId) {
        // 1. Verificar evento
        Evento evento = eventoRepository.findById(request.getEventoId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", request.getEventoId()));

        Long competicionId = evento.getCompeticion().getId();
        Long deporteId = evento.getCompeticion().getDeporte().getId();

        // 2. Verificar jugador + pertenencia a uno de los equipos del evento
        Jugador jugador = jugadorRepository.findById(request.getJugadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador", "id", request.getJugadorId()));

        List<Long> equiposDelEvento = eventoEquipoRepository.findByEventoId(evento.getId())
                .stream()
                .map(ee -> ee.getEquipo().getId())
                .toList();

        if (equiposDelEvento.isEmpty()) {
            throw new BadRequestException("El evento no tiene equipos asignados");
        }

        Long equipoDelJugador = equiposDelEvento.stream()
                .filter(equipoId -> equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(equipoId, jugador.getId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "El jugador no está inscrito en ninguno de los equipos del evento"));

        // 3. Verificar tipo de estadística + coincidencia con el deporte de la competición
        TipoEstadistica tipoEstadistica = tipoEstadisticaRepository.findById(request.getTipoEstadisticaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo estadística", "id", request.getTipoEstadisticaId()));

        if (tipoEstadistica.getDeporte() == null
                || !deporteId.equals(tipoEstadistica.getDeporte().getId())) {
            throw new BadRequestException(
                    "El tipo de estadística no pertenece al deporte de la competición del evento");
        }

        // 4. Verificar permisos del usuario en la competición
        if (!usuarioTienePermisoRegistrar(usuarioId, competicionId, equipoDelJugador)) {
            throw new UnauthorizedException(
                    "El usuario no tiene permiso para registrar estadísticas en esta competición");
        }

        // 5. Validar coherencia del valor con el tipo
        validarValorSegunTipo(request.getValor(), tipoEstadistica);

        // 6. Upsert: si existe, actualiza; si no, crea
        Optional<EstadisticaJugadorEvento> existente = estadisticaRepository
                .findByEventoIdAndJugadorIdAndTipoEstadisticaId(
                        evento.getId(), jugador.getId(), tipoEstadistica.getId());

        boolean esCreacion = existente.isEmpty();
        EstadisticaJugadorEvento estadistica = existente.orElseGet(() -> EstadisticaJugadorEvento.builder()
                .evento(evento)
                .jugador(jugador)
                .tipoEstadistica(tipoEstadistica)
                .build());

        estadistica.setValor(BigDecimal.valueOf(request.getValor()));
        estadistica = estadisticaRepository.save(estadistica);

        // 7. Registrar log (CREAR o EDITAR según corresponda)
        LogModificacion.AccionLog accion = esCreacion
                ? LogModificacion.AccionLog.CREAR
                : LogModificacion.AccionLog.EDITAR;
        logService.registrar("Estadistica", estadistica.getId(), accion, null, null, competicionId);

        // Nota: recálculo de EstadisticaAcumulada se difiere al endpoint
        // /estadisticas/competicion/{id}/jugador/{id}/acumulado que ya agrega on-the-fly.

        return estadisticaMapper.toDTO(estadistica);
    }

    /**
     * Comprueba si el usuario tiene permiso para registrar estadísticas en la competición.
     * <p>
     * Tiene permiso si:
     * <ul>
     *   <li>es {@code ADMIN_SISTEMA} global, o</li>
     *   <li>posee alguno de los roles {@code ADMIN_COMPETICION} o {@code ARBITRO}
     *       en la competición (vía {@link com.compapption.api.entity.UsuarioRolCompeticion}), o</li>
     *   <li>es {@code MANAGER_EQUIPO} del equipo del jugador en esa competición
     *       (vía {@link com.compapption.api.entity.EquipoManager}).</li>
     * </ul>
     *
     * @param usuarioId       identificador del usuario
     * @param competicionId   identificador de la competición
     * @param equipoDelJugador identificador del equipo del jugador dentro del evento
     * @return {@code true} si el usuario tiene permiso para registrar estadísticas
     */
    private boolean usuarioTienePermisoRegistrar(Long usuarioId, Long competicionId, Long equipoDelJugador) {
        if (usuarioId == null) return false;

        // ADMIN_SISTEMA global
        boolean esAdminSistema = usuarioRepository.findById(usuarioId)
                .map(u -> Boolean.TRUE.equals(u.getEsAdminSistema()))
                .orElse(false);
        if (esAdminSistema) return true;

        // Rol en la competición: ADMIN_COMPETICION / ARBITRO
        boolean tieneRolEnCompeticion = usuarioRolCompeticionRepository
                .existsByUsuarioIdAndCompeticionIdAndRolNombreIn(
                        usuarioId,
                        competicionId,
                        List.of(Rol.RolNombre.ADMIN_COMPETICION, Rol.RolNombre.ARBITRO));
        if (tieneRolEnCompeticion) return true;

        // Manager del equipo del jugador en la competición
        return equipoManagerRepository
                .existsByEquipoIdAndCompeticionIdAndUsuarioId(equipoDelJugador, competicionId, usuarioId);
    }

    /**
     * Valida que el valor propuesto sea coherente con el tipo de estadística:
     * ENTERO rechaza decimales, BOOLEANO solo acepta 0 o 1, DECIMAL y TIEMPO aceptan
     * cualquier valor no negativo.
     *
     * @param valor           valor numérico a validar (no {@code null})
     * @param tipoEstadistica tipo de estadística con el {@code TipoValor} asociado
     * @throws BadRequestException si el valor no cumple las restricciones del tipo
     */
    private void validarValorSegunTipo(Double valor, TipoEstadistica tipoEstadistica) {
        if (valor == null) {
            throw new BadRequestException("El valor de la estadística no puede ser nulo");
        }
        TipoEstadistica.TipoValor tipo = tipoEstadistica.getTipoValor();
        switch (tipo) {
            case ENTERO -> {
                if (valor % 1 != 0) {
                    throw new BadRequestException(
                            "El tipo de estadística '" + tipoEstadistica.getNombre()
                                    + "' es ENTERO y no admite valores decimales");
                }
                if (valor < 0) {
                    throw new BadRequestException(
                            "El valor de una estadística ENTERO no puede ser negativo");
                }
            }
            case BOOLEANO -> {
                if (valor != 0.0 && valor != 1.0) {
                    throw new BadRequestException(
                            "El tipo de estadística '" + tipoEstadistica.getNombre()
                                    + "' es BOOLEANO: solo se aceptan los valores 0 o 1");
                }
            }
            case DECIMAL -> {
                if (valor < 0) {
                    throw new BadRequestException(
                            "El valor de una estadística DECIMAL no puede ser negativo");
                }
            }
            case TIEMPO -> {
                if (valor < 0) {
                    throw new BadRequestException(
                            "El valor de una estadística TIEMPO no puede ser negativo");
                }
            }
        }
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
