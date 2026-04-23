package com.compapption.api.service;

import com.compapption.api.dto.UsuarioRolCompeticion.UsuarioRolCompeticionDTO;
import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.dto.competicionDTO.CompeticionInfoDTO;
import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.mapper.CompeticionMapper;
import com.compapption.api.mapper.EquipoMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.competicion.CompeticionCreateRequest;
import com.compapption.api.request.competicion.CompeticionUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.compapption.api.service.log.LogService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Servicio que gestiona el ciclo de vida completo de las competiciones. Implementa la lógica
 * de creación, edición, eliminación, cambio de estado, gestión de equipos inscritos y
 * administración de usuarios con roles. Depende de {@link ClasificacionService} para
 * inicializar clasificaciones, {@link ConfiguracionCompeticionService} para la configuración
 * del formato, {@link UsuarioRolCompeticionService} para el control de acceso y
 * {@link LogService} para la auditoría de operaciones.
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class CompeticionService {

    private final CompeticionRepository competicionRepository;
    private final DeporteRepository deporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompeticionEquipoRepository competicionEquipoRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    private final CompeticionMapper competicionMapper;
    private final EquipoMapper equipoMapper;
    private final ClasificacionService clasificacionService;
    private final ConfiguracionCompeticionService configuracionCompeticionService;
    private final UsuarioRolCompeticionService usuarioRolCompeticionService;
    private final LogService logService;
    private final NotificacionService notificacionService;

    // === CRUD COMPETICIONES === //

    // Consultas //

    /**
     * Devuelve una página de competiciones públicas y activas. Solo se incluyen las
     * competiciones con visibilidad pública y en estado no finalizado.
     *
     * @param pageable parámetros de paginación y ordenación
     * @return página con la lista de competiciones públicas en formato simple
     */
    @Transactional(readOnly = true)
    public PageResponse<CompeticionSimpleDTO> obtenerPublicas(Pageable pageable){
        Page<Competicion> page = competicionRepository.findByPublicasActivas(pageable);
        return toPageResponseSimple(page);
    }

    /**
     * Busca competiciones públicas cuyo nombre contenga el texto indicado.
     *
     * @param search texto a buscar en el nombre de la competición
     * @param pageable parámetros de paginación y ordenación
     * @return página con los resultados de la búsqueda en formato simple
     */
    @Transactional(readOnly = true)
    public PageResponse<CompeticionSimpleDTO> buscarPublicas(String search, Pageable pageable){
        Page<Competicion> page = competicionRepository.searchPublicas(search, pageable);
        return toPageResponseSimple(page);
    }

    /**
     * Obtiene el detalle completo de una competición por su identificador.
     *
     * @param id identificador de la competición
     * @return DTO con todos los datos de la competición, incluida la configuración
     * @throws ResourceNotFoundException si no existe ninguna competición con el id indicado
     */
    @Transactional(readOnly = true)
    public CompeticionDetalleDTO obtenerPorIdDetalle(Long id){
        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", id));
        return competicionMapper.toDetalleDTO(competicion);
    }

    /**
     * Obtiene la información básica de una competición por su identificador.
     *
     * @param id identificador de la competición
     * @return DTO de información con los campos principales de la competición
     * @throws ResourceNotFoundException si no existe ninguna competición con el id indicado
     */
    @Transactional(readOnly = true)
    public CompeticionInfoDTO obtenerPorIdInfo(Long id){
        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", id));
        return competicionMapper.toInfoDTO(competicion);
    }

    /**
     * Obtiene el resumen simple de una competición por su identificador.
     *
     * @param id identificador de la competición
     * @return DTO simple con los campos mínimos de la competición
     * @throws ResourceNotFoundException si no existe ninguna competición con el id indicado
     */
    @Transactional(readOnly = true)
    public CompeticionSimpleDTO obtenerPorIdSimple(Long id){
        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", id));
        return competicionMapper.toSimpleDTO(competicion);
    }

    /**
     * Lista todas las competiciones en las que un usuario participa como miembro
     * (cualquier rol: jugador, manager o administrador).
     *
     * @param usuarioId identificador del usuario participante
     * @return lista de competiciones en formato simple
     */
    @Transactional(readOnly = true)
    public List<CompeticionSimpleDTO> obtenerPorParticipante(Long usuarioId){
        List<Competicion> competiciones = competicionRepository.findByUsuarioParticipante(usuarioId);
        return competicionMapper.toSimpleDTOList(competiciones);
    }

    /**
     * Lista todas las competiciones creadas por un usuario concreto.
     *
     * @param usuarioId identificador del usuario creador
     * @return lista de competiciones en formato simple
     */
    @Transactional(readOnly = true)
    public List<CompeticionSimpleDTO> obtenerPorCreador(Long usuarioId){
        List<Competicion> competiciones = competicionRepository.findByCreadorId(usuarioId);
        return competicionMapper.toSimpleDTOList(competiciones);
    }

    // Creación //

    /**
     * Crea una nueva competición en estado BORRADOR. Genera automáticamente la configuración
     * asociada con los parámetros del request y asigna al creador el rol ADMIN_COMPETICION.
     * La operación queda registrada en el log de auditoría.
     *
     * @param request datos de la nueva competición, incluida la configuración de formato
     * @param creadorId identificador del usuario que crea la competición
     * @return DTO con el detalle completo de la competición creada
     * @throws ResourceNotFoundException si el deporte o el usuario creador no existen
     */
    @Transactional
    public CompeticionDetalleDTO crear(CompeticionCreateRequest request, Long creadorId){
        Deporte deporte = deporteRepository.findById(request.getDeporteId())
                .orElseThrow(()-> new ResourceNotFoundException("Deporte", "id", request.getDeporteId()));

        Usuario creador = usuarioRepository.findById(creadorId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", creadorId));

        Competicion competicion = Competicion.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .deporte(deporte)
                .creador(creador)
                .publica(request.isPublica())
                .inscripcionAbierta(request.getInscripcionAbierta() == null || request.getInscripcionAbierta())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(Competicion.EstadoCompeticion.BORRADOR)
                .build();

        competicion = competicionRepository.save(competicion);

        // Crear configuración
        ConfiguracionCompeticion configuracion = configuracionCompeticionService.crear(
                competicion,
                request.getConfiguracion());
        competicion.setConfiguracion(configuracion);

        // Asignar rol de administrador
        usuarioRolCompeticionService.asignarRolAdminCompeticion(creador, competicion);

        logService.registrar("Competicion", competicion.getId(), LogModificacion.AccionLog.CREAR, null, null, competicion.getId());
        return competicionMapper.toDetalleDTO(competicion);
    }

    // Actualización //

    /**
     * Actualiza los datos de una competición existente. Solo se aplican los campos no nulos
     * del request (actualización parcial). Si se incluye un cambio de estado, se validan las
     * reglas de transición correspondientes. Registra la operación en el log de auditoría.
     *
     * @param id identificador de la competición a actualizar
     * @param request datos a modificar; los campos nulos no se tocan
     * @param usuarioId identificador del usuario que realiza la operación
     * @return DTO con el detalle actualizado de la competición
     * @throws ResourceNotFoundException si la competición no existe
     * @throws BadRequestException si el usuario no tiene permisos o la transición de estado no es válida
     */
    @Transactional
    public CompeticionDetalleDTO actualizar(
            Long id,
            CompeticionUpdateRequest request,
            Long usuarioId) throws BadRequestException {

        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", id));

        validarPermisoEdicion(competicion, usuarioId);

        if (request.getNombre() != null){
            competicion.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null){
            competicion.setDescripcion(request.getDescripcion());
        }
        if (request.getPublica() != null) competicion.setPublica(request.getPublica());
        if (request.getInscripcionAbierta() != null) competicion.setInscripcionAbierta(request.getInscripcionAbierta());
        if (request.getEstadisticasActivas() != null) competicion.setEstadisticasActivas(request.getEstadisticasActivas());
        if (request.getFechaInicio() != null){
            competicion.setFechaInicio(request.getFechaInicio());
        }
        if (request.getFechaFin() != null){
            competicion.setFechaFin(request.getFechaFin());
        }
        Competicion.EstadoCompeticion estadoAnterior = competicion.getEstado();
        boolean activando = false;
        if (request.getEstado() != null){
            validarCambioEstado(competicion, request.getEstado());
            activando = estadoAnterior != Competicion.EstadoCompeticion.ACTIVA
                    && request.getEstado() == Competicion.EstadoCompeticion.ACTIVA;
            competicion.setEstado(request.getEstado());
        }
        if (request.getConfiguracion() != null){
            configuracionCompeticionService.actualizar(
                    competicion.getConfiguracion(),
                    request.getConfiguracion());
        }

        competicion = competicionRepository.save(competicion);
        logService.registrar("Competicion", competicion.getId(), LogModificacion.AccionLog.EDITAR, null, null, competicion.getId());

        if (activando) {
            notificarCompeticionActivada(competicion);
        }

        return competicionMapper.toDetalleDTO(competicion);
    }

    // Eliminación

    /**
     * Elimina permanentemente una competición. No se puede eliminar si está en estado ACTIVA.
     * Antes de eliminar se limpian las referencias de log para evitar constraint violations,
     * y la operación queda registrada en el log de auditoría.
     *
     * @param id identificador de la competición a eliminar
     * @param usuarioId identificador del usuario que solicita la eliminación
     * @throws ResourceNotFoundException si la competición no existe
     * @throws BadRequestException si la competición está en estado ACTIVA o el usuario no tiene permisos
     */
    @Transactional
    public void eliminar(long id, long usuarioId) {
        Competicion competicion = competicionRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", id));

        validarPermisoEdicion(competicion, usuarioId);

        if (competicion.getEstado() == Competicion.EstadoCompeticion.ACTIVA) {
            throw new BadRequestException("No se puede eliminar una competición activa");
        }

        logService.registrar("Competicion", id, LogModificacion.AccionLog.ELIMINAR, null, null, null);
        logService.clearCompeticion(id);
        competicionRepository.delete(competicion);
    }

    // Cambio de temporada

    /**
     * Avanza la competición a una nueva temporada. La nueva temporada debe ser
     * numéricamente superior a la actual. Inicializa automáticamente las clasificaciones
     * a cero para todos los equipos activos en la nueva temporada.
     *
     * @param competicionId identificador de la competición
     * @param nuevaTemporada número de la nueva temporada (debe ser mayor a la actual)
     * @param usuarioId identificador del usuario que realiza la operación
     * @return DTO con el detalle de la competición actualizada
     * @throws ResourceNotFoundException si la competición no existe
     * @throws BadRequestException si la nueva temporada no es mayor a la actual o el usuario no tiene permisos
     */
    @Transactional
    public CompeticionDetalleDTO cambiarTemporada(Long competicionId, Integer nuevaTemporada, Long usuarioId) {
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competicion", "id", competicionId));

        validarPermisoEdicion(competicion, usuarioId);

        if (nuevaTemporada <= competicion.getTemporadaActual()) {
            throw new BadRequestException("La nueva temporada debe ser mayor a la actual");
        }

        competicion.setTemporadaActual(nuevaTemporada);
        competicion = competicionRepository.save(competicion);

        // Inicializar clasificaciones para la nueva temporada
        Competicion finalCompeticion = competicion;
        competicionEquipoRepository.findActivosByCompeticionId(competicionId)
                .forEach(ce -> clasificacionService
                        .inicializarClasificacionEquipo(finalCompeticion, ce.getEquipo()));

        // TODO: generación de calendario, gestión de inscripciones, etc.

        return competicionMapper.toDetalleDTO(competicion);
    }

    // Gestión de equipos

    /**
     * Inscribe un equipo en una competición. Las inscripciones deben estar abiertas y el
     * equipo no debe estar ya inscrito. Al inscribir, se inicializa automáticamente su
     * entrada en la tabla de clasificación con todos los contadores a cero.
     *
     * @param competicionId identificador de la competición
     * @param equipoId identificador del equipo a inscribir
     * @param usuarioId identificador del usuario que realiza la operación
     * @throws ResourceNotFoundException si la competición o el equipo no existen
     * @throws BadRequestException si el equipo ya está inscrito, las inscripciones están cerradas
     *                             o el usuario no tiene permisos
     */
    @Transactional
    public void altaEquipo(long competicionId, long equipoId, long usuarioId) {
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", competicionId));

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", equipoId));

        if (competicionEquipoRepository.existsByCompeticionIdAndEquipoId(competicionId, equipoId)) {
            throw new BadRequestException("El equipo ya está inscrito en esta competición");
        }

        validarPermisoEdicion(competicion, usuarioId);

        if (competicion.getEstado() != Competicion.EstadoCompeticion.BORRADOR
                && !competicion.isInscripcionAbierta()) {
            throw new BadRequestException("Las inscripciones están cerradas");
        }

        CompeticionEquipo inscripcion = CompeticionEquipo.builder()
                .competicion(competicion)
                .equipo(equipo)
                .activo(true)
                .build();

        competicionEquipoRepository.save(inscripcion);

        // Inicializar clasificación para el nuevo equipo
        clasificacionService.inicializarClasificacionEquipo(competicion, equipo);
        logService.registrar("CompeticionEquipo", equipoId, LogModificacion.AccionLog.CREAR, null, null, competicionId);
    }

    /**
     * Da de baja a un equipo de una competición marcando su inscripción como inactiva.
     * La inscripción no se elimina físicamente para preservar el historial.
     *
     * @param competicionId identificador de la competición
     * @param equipoId identificador del equipo a dar de baja
     * @param usuarioId identificador del usuario que realiza la operación
     * @throws ResourceNotFoundException si la competición o la inscripción no existen
     * @throws BadRequestException si el usuario no tiene permisos de edición
     */
    @Transactional
    public void bajaEquipo(long competicionId, long equipoId, long usuarioId){
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));

        validarPermisoEdicion(competicion, usuarioId);

        CompeticionEquipo inscripcion = competicionEquipoRepository
                .findByCompeticionIdAndEquipoId(competicionId, equipoId)
                .orElseThrow(()-> new ResourceNotFoundException("Inscripción no encontrada"));

        inscripcion.setActivo(false);
        competicionEquipoRepository.save(inscripcion);
        logService.registrar("CompeticionEquipo", equipoId, LogModificacion.AccionLog.EDITAR, null, null, competicionId);
    }

    /**
     * Devuelve la lista de equipos actualmente inscritos y activos en una competición
     * en formato simple (campos mínimos para listado).
     *
     * @param competicionId identificador de la competición
     * @return lista de equipos inscritos en formato simple
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional
    public List<EquipoSimpleDTO> obtenerInscritosSimple(long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }

        return competicionEquipoRepository.findActivosByCompeticionId(competicionId)
                .stream()
                .map(ce -> equipoMapper.toSimpleDTO(ce.getEquipo()))
                .toList();
    }

    /**
     * Devuelve la lista de equipos actualmente inscritos y activos en una competición
     * en formato detalle (todos los campos, incluidos jugadores).
     *
     * @param competicionId identificador de la competición
     * @return lista de equipos inscritos en formato detalle
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional
    public List<EquipoDetalleDTO> obtenerInscritosDetalle(long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }

        return competicionEquipoRepository.findActivosByCompeticionId(competicionId)
                .stream()
                .map(ce -> equipoMapper.toDetalleDTO(ce.getEquipo()))
                .toList();
    }

    // === CONVERSIÓN DTOS A PAGE === //

    /**
     * Convierte una página JPA de entidades Competicion en un PageResponse de DTOs simples.
     *
     * @param page página de entidades devuelta por el repositorio
     * @return respuesta de página con DTOs simples y metadatos de paginación
     */
    private PageResponse<CompeticionSimpleDTO> toPageResponseSimple(Page<Competicion> page) {
        return PageResponse.<CompeticionSimpleDTO>builder()
                .content(competicionMapper.toSimpleDTOList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * Convierte una página JPA de entidades Competicion en un PageResponse de DTOs detalle.
     *
     * @param page página de entidades devuelta por el repositorio
     * @return respuesta de página con DTOs detalle y metadatos de paginación
     */
    private PageResponse<CompeticionDetalleDTO> toPageResponseDetalle(Page<Competicion> page) {
        return PageResponse.<CompeticionDetalleDTO>builder()
                .content(competicionMapper.toDetalleDTOList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    // === VALIDACIONES ESPECÍFICAS DE COMPETICIONES === //

    /**
     * Valida que la transición al nuevo estado sea permitida según las reglas de negocio.
     * Para activar una liga se requieren mínimo 3 equipos; para playoff o evento único,
     * mínimo 2 equipos.
     *
     * @param competicion entidad de la competición a validar
     * @param nuevoEstado estado al que se quiere transicionar
     * @throws BadRequestException si no se cumplen los requisitos mínimos de equipos
     */
    private void validarCambioEstado(Competicion competicion, Competicion.EstadoCompeticion nuevoEstado) {
        if (nuevoEstado == Competicion.EstadoCompeticion.ACTIVA) {
            long numEquipos = competicionEquipoRepository.countActivosByCompeticionId(competicion.getId());
            ConfiguracionCompeticion config = competicion.getConfiguracion();
            ConfiguracionCompeticion.FormatoCompeticion formato = config != null
                    ? config.getFormato()
                    : ConfiguracionCompeticion.FormatoCompeticion.LIGA;

            if (formato == ConfiguracionCompeticion.FormatoCompeticion.EVENTO_UNICO) {
                if (numEquipos < 2) {
                    throw new BadRequestException("Un evento único necesita al menos 2 equipos");
                }
            } else if (formato == ConfiguracionCompeticion.FormatoCompeticion.PLAYOFF) {
                if (numEquipos < 2) {
                    throw new BadRequestException("Un playoff necesita al menos 2 equipos");
                }
            } else {
                // Ligas y formatos con fase de liga requieren mínimo 3
                if (numEquipos < 3) {
                    throw new BadRequestException("Una liga necesita al menos 3 equipos para activarse");
                }
            }
        }
    }

    // === PERMISOS DE EDICIÓN === //

    /**
     * Verifica que el usuario tenga permiso para editar la competición. Tienen permiso el
     * creador original o cualquier usuario con el rol ADMIN_COMPETICION en dicha competición.
     *
     * @param competicion entidad de la competición a proteger
     * @param usuarioId identificador del usuario que intenta la operación
     * @throws BadRequestException si el usuario no es el creador ni tiene rol de administrador
     */
    private void validarPermisoEdicion(Competicion competicion, long usuarioId) {
        if (!Objects.equals(competicion.getCreador().getId(), usuarioId) &&
                !usuarioRolCompeticionService.tieneRol(
                        usuarioId, competicion.getId(), Rol.RolNombre.ADMIN_COMPETICION)) {
            throw new BadRequestException("No tienes permisos para editar esta competición");
        }
    }

    // ==================== ESTADO ====================

    /**
     * Cambia el estado de una competición de forma explícita. Se validan las reglas de
     * negocio para el nuevo estado antes de aplicar el cambio.
     *
     * @param id identificador de la competición
     * @param nuevoEstado nuevo estado al que se quiere cambiar la competición
     * @param usuarioId identificador del usuario que realiza la operación
     * @return DTO con el detalle de la competición con el estado actualizado
     * @throws ResourceNotFoundException si la competición no existe
     * @throws BadRequestException si el usuario no tiene permisos o el cambio de estado no es válido
     */
    @Transactional
    public CompeticionDetalleDTO cambiarEstado(Long id, Competicion.EstadoCompeticion
                                                       nuevoEstado,
                                               Long usuarioId) {
        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competición", "id", id));

        validarPermisoEdicion(competicion, usuarioId);
        validarCambioEstado(competicion, nuevoEstado);

        Competicion.EstadoCompeticion estadoAnterior = competicion.getEstado();
        competicion.setEstado(nuevoEstado);
        Competicion actualizada = competicionRepository.save(competicion);

        // Notificar a todos los miembros cuando la competición pasa a ACTIVA
        if (estadoAnterior != Competicion.EstadoCompeticion.ACTIVA
                && nuevoEstado == Competicion.EstadoCompeticion.ACTIVA) {
            notificarCompeticionActivada(actualizada);
        }

        return competicionMapper.toDetalleDTO(actualizada);
    }

    /**
     * Notifica a todos los usuarios con algún rol en la competición que esta ha sido activada.
     * Deduplica por usuarioId para evitar múltiples notificaciones si un usuario tiene varios roles.
     *
     * @param competicion competición recién activada
     */
    private void notificarCompeticionActivada(Competicion competicion) {
        Set<Long> destinatarios = new HashSet<>();
        usuarioRolCompeticionRepository.findByCompeticionId(competicion.getId())
                .forEach(urc -> {
                    if (urc.getUsuario() != null) destinatarios.add(urc.getUsuario().getId());
                });

        for (Long uid : destinatarios) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("competicionId", competicion.getId());
            payload.put("competicionNombre", competicion.getNombre());
            notificacionService.crear(uid,
                    Notificacion.TipoNotificacion.COMPETICION_ACTIVADA, payload);
        }
    }

    // ==================== GESTIÓN DE USUARIOS ====================

    /**
     * Devuelve la lista de todos los usuarios con algún rol asignado en la competición,
     * incluyendo su nombre de usuario, email y el nombre del rol.
     *
     * @param competicionId identificador de la competición
     * @return lista de usuarios con sus roles en la competición
     * @throws ResourceNotFoundException si la competición no existe
     */
    @Transactional(readOnly = true)
    public List<UsuarioRolCompeticionDTO> obtenerUsuariosConRol(Long competicionId) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return usuarioRolCompeticionService.obtenerMiembros(competicionId)
                .stream()
                .map(urc -> UsuarioRolCompeticionDTO.builder()
                        .usuarioId(urc.getUsuario().getId())
                        .username(urc.getUsuario().getUsername())
                        .email(urc.getUsuario().getEmail())
                        .rolNombre(urc.getRol().getNombre().name())
                        .fechaAsignacion(urc.getFechaAsignacion())
                        .build())
                .toList();
    }

    /**
     * Revoca todos los roles de un usuario en una competición, expulsándolo efectivamente
     * de la misma. Solo el creador o un administrador de la competición puede realizar
     * esta operación.
     *
     * @param competicionId identificador de la competición
     * @param usuarioId identificador del usuario a expulsar
     * @param solicitanteId identificador del usuario que solicita la operación
     * @throws ResourceNotFoundException si la competición no existe o el usuario no tiene roles en ella
     * @throws BadRequestException si el solicitante no tiene permisos de administración
     */
    @Transactional
    public void quitarUsuario(Long competicionId, Long usuarioId, Long solicitanteId) {
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competición", "id",
                        competicionId));

        validarPermisoEdicion(competicion, solicitanteId);

        if (usuarioRolCompeticionService.obtenerRolesDeUsuario(usuarioId,
                competicionId).isEmpty()) {
            throw new ResourceNotFoundException("El usuario no tiene roles en esta competición");
        }
        usuarioRolCompeticionService.revocarTodosLosRoles(usuarioId, competicionId);
    }
}
