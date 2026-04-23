package com.compapption.api.service;

import com.compapption.api.dto.invitacionDTO.InvitacionDetalleDTO;
import com.compapption.api.dto.invitacionDTO.InvitacionSimpleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.InvitacionMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.invitacion.InvitacionCreateRequest;
import com.compapption.api.service.log.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para la gestión completa del ciclo de vida de las invitaciones.
 * <p>
 * Cubre la creación de invitaciones con token UUID, el envío del email de notificación,
 * la aceptación (que asigna el rol y/o vincula al usuario al equipo/competición)
 * y el rechazo. También proporciona consultas por usuario, emisor y competición,
 * y un método de mantenimiento para marcar las invitaciones expiradas.
 * </p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class InvitacionService {

    private final InvitacionRepository invitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompeticionRepository competicionRepository;
    private final EquipoRepository equipoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    private final EquipoManagerRepository equipoManagerRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;
    private final JugadorRepository jugadorRepository;
    private final EmailService emailService;
    private final InvitacionMapper invitacionMapper;
    private final LogService logService;
    private final NotificacionService notificacionService;

    public static final List<String> ROLES_VALIDOS = List.of("ADMIN_COMPETICION", "MANAGER_EQUIPO", "JUGADOR");

    /// === CREACIÓN DE INVITACIÓN === ///

    /**
     * Crea una nueva invitación y envía el email de notificación al destinatario.
     * <p>
     * Valida que el rol ofrecido sea válido, que no exista ya una invitación pendiente
     * para el mismo email en la misma competición/equipo, y genera un token UUID único
     * con expiración de 7 días. Si el destinatario ya está registrado, se vincula
     * directamente; en caso contrario, se vincula al aceptar.
     * </p>
     *
     * @param emisorId identificador del usuario que emite la invitación
     * @param request  datos de la invitación: email destinatario, rol, competición y/o equipo
     * @return DTO detalle de la invitación creada
     * @throws com.compapption.api.exception.BadRequestException       si el rol no es válido,
     *                                                                  faltan campos obligatorios
     *                                                                  o ya existe una invitación pendiente
     * @throws com.compapption.api.exception.ResourceNotFoundException  si el emisor, la competición
     *                                                                  o el equipo no existen
     */
    @Transactional
    public InvitacionDetalleDTO crearInvitacion(Long emisorId, InvitacionCreateRequest request) {
        String rol = request.getRolOfrecido();

        if (!ROLES_VALIDOS.contains(rol)) {
            throw new BadRequestException("Rol ofrecido no válido: " + rol + ". Valores permitidos: " + ROLES_VALIDOS);
        }
        if (("ADMIN_COMPETICION".equals(rol) || "MANAGER_EQUIPO".equals(rol)) && request.getCompeticionId() == null) {
            throw new BadRequestException("Se requiere id de la competicion para invitar como " + rol);
        }
        if (("MANAGER_EQUIPO".equals(rol) || "JUGADOR".equals(rol)) && request.getEquipoId() == null) {
            throw new BadRequestException("Se requiere id del equipo para invitar como " + rol);
        }

        Usuario emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", emisorId));

        Competicion competicion = null;
        if (request.getCompeticionId()!=null) {
            competicion = competicionRepository.findById(request.getCompeticionId())
                    .orElseThrow(()->  new ResourceNotFoundException("Competicion", "id", request.getCompeticionId()));
        }

        Equipo equipo = null;
        if (request.getEquipoId()!=null) {
            equipo = equipoRepository.findById(request.getEquipoId())
                    .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", request.getEquipoId()));
        }

        // Verificar duplicados para invitaciones de competición
        if (competicion != null && invitacionRepository.existsByDestinatarioEmailAndCompeticionIdAndEstado(
                request.getDestinatarioEmail(), request.getCompeticionId(), Invitacion.EstadoInvitacion.PENDIENTE)) {
            throw new BadRequestException("Ya existe una invitación pendiente para ese email en esta competición");
        }

        // Verificar duplicado para invitaciones de equipo (JUGADOR)
        if ("JUGADOR".equals(rol) && equipo != null && invitacionRepository.existsByDestinatarioEmailAndEquipoIdAndEstado(
                request.getDestinatarioEmail(), request.getEquipoId(), Invitacion.EstadoInvitacion.PENDIENTE)) {
            throw new BadRequestException("Ya existe una invitación pendiente para ese email en este equipo");
        }

        // Vincular destinatario si ya está registrado
        Usuario destinatario = usuarioRepository.findByEmail(request.getDestinatarioEmail()).orElse(null);

        String token = UUID.randomUUID().toString();

        Invitacion invitacion = Invitacion.builder()
                .emisor(emisor)
                .destinatarioEmail(request.getDestinatarioEmail())
                .destinatario(destinatario)
                .competicion(competicion)
                .equipo(equipo)
                .rolOfrecido(rol)
                .token(token)
                .fechaExpiracion(LocalDateTime.now().plusDays(7))
                .build();

        invitacion = invitacionRepository.save(invitacion);
        logService.registrar("Invitacion", invitacion.getId(), LogModificacion.AccionLog.CREAR, null, null,
                competicion != null ? competicion.getId() : null);

        // Enviar email de forma asíncrona
        String nombreContexto = competicion != null ? competicion.getNombre()
                : (equipo!=null ? equipo.getNombre() : emisor.getUsername());
        emailService.enviarInvitacion(request.getDestinatarioEmail(), nombreContexto, nombreContexto, rol, token);

        // Notificar al destinatario si ya es usuario del sistema
        if (destinatario != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("invitacionId", invitacion.getId());
            payload.put("competicionNombre", competicion != null ? competicion.getNombre() : null);
            notificacionService.crear(destinatario.getId(),
                    Notificacion.TipoNotificacion.INVITACION_RECIBIDA, payload);
        }

        return invitacionMapper.toDetalleDTO(invitacion);
    }

    /// === ACEPTAR Y RECHAZAR INVITACIÓN === ///

    /**
     * Acepta una invitación identificada por su token y asigna el rol correspondiente al usuario.
     * <p>
     * Según el rol ofrecido, puede: asignar {@code ADMIN_COMPETICION} en la competición,
     * asignar {@code MANAGER_EQUIPO} y vincular al equipo, o crear/obtener el jugador
     * y añadirlo al equipo. La invitación pasa a estado {@code ACEPTADA}.
     * </p>
     *
     * @param token     token UUID de la invitación
     * @param usuarioId identificador del usuario que acepta
     * @return DTO detalle de la invitación actualizada
     * @throws com.compapption.api.exception.BadRequestException      si la invitación no está pendiente,
     *                                                                 ha expirado, o el rol es desconocido
     * @throws com.compapption.api.exception.ResourceNotFoundException si el token o el usuario no existen
     */
    @Transactional
    public InvitacionDetalleDTO aceptarPorToken(String token, Long usuarioId) {
        Invitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(()-> new ResourceNotFoundException("Invitación", "token", token));

        if (invitacion.getEstado()!= Invitacion.EstadoInvitacion.PENDIENTE) {
            throw new BadRequestException("La invitación no está pendiente (estado actual: " + invitacion.getEstado() + ")");
        }
        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            invitacion.setEstado(Invitacion.EstadoInvitacion.EXPIRADA);
            invitacionRepository.save(invitacion);
            throw new BadRequestException("La invitación ha expirado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", usuarioId));

        switch (invitacion.getRolOfrecido()) {
            case "ADMIN_COMPETICION" -> {
                if (invitacion.getCompeticion() == null) {
                    throw new BadRequestException("La invitación no tiene competición asociada");
                }
                asignarRolEncompeticion(usuario, invitacion.getCompeticion(), Rol.RolNombre.ADMIN_COMPETICION);
            }
            case "MANAGER_EQUIPO" -> {
                if (invitacion.getCompeticion()==null || invitacion.getEquipo()==null) {
                    throw new BadRequestException("La invitación requiere competición y equipo");
                }
                asignarRolEncompeticion(usuario, invitacion.getCompeticion(), Rol.RolNombre.MANAGER_EQUIPO);
                asignarManagerEquipo(usuario, invitacion.getEquipo(), invitacion.getCompeticion());
            }
            case "JUGADOR"-> {
                if (invitacion.getEquipo()==null) {
                    throw new BadRequestException("La invitación no tiene equipo asociado");
                }
                Jugador jugador = obtenerOCrearJugador(usuario);
                agregarJugadorAEquipo(jugador, invitacion.getEquipo());
            }
            default -> throw new BadRequestException("Rol ogrecido desconocido: " + invitacion.getRolOfrecido());
        }

        invitacion.setEstado(Invitacion.EstadoInvitacion.ACEPTADA);
        invitacion.setDestinatario(usuario);
        invitacion = invitacionRepository.save(invitacion);
        logService.registrar("Invitacion", invitacion.getId(), LogModificacion.AccionLog.EDITAR, null, null,
                invitacion.getCompeticion() != null ? invitacion.getCompeticion().getId() : null);

        // Notificar al creador de la competición cuando un equipo es aceptado (MANAGER_EQUIPO)
        if ("MANAGER_EQUIPO".equals(invitacion.getRolOfrecido())
                && invitacion.getCompeticion() != null
                && invitacion.getCompeticion().getCreador() != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("equipoNombre", invitacion.getEquipo() != null ? invitacion.getEquipo().getNombre() : null);
            payload.put("competicionNombre", invitacion.getCompeticion().getNombre());
            notificacionService.crear(invitacion.getCompeticion().getCreador().getId(),
                    Notificacion.TipoNotificacion.EQUIPO_ACEPTADO, payload);
        }

        return invitacionMapper.toDetalleDTO(invitacion);
    }

    /**
     * Rechaza una invitación identificada por su token y la marca como {@code RECHAZADA}.
     *
     * @param token     token UUID de la invitación
     * @param usuarioId identificador del usuario que rechaza
     * @return DTO detalle de la invitación actualizada
     * @throws com.compapption.api.exception.BadRequestException      si la invitación no está pendiente
     *                                                                 o ha expirado
     * @throws com.compapption.api.exception.ResourceNotFoundException si el token no existe
     */
    @Transactional
    public InvitacionDetalleDTO rechazarPorToken(String token, Long usuarioId) {
        Invitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(()-> new ResourceNotFoundException("Invitación", "token", token));

        if (invitacion.getEstado() != Invitacion.EstadoInvitacion.PENDIENTE) {
            throw new BadRequestException("La invitación no está pendiente (estado actual: " + invitacion.getEstado() + ")");
        }
        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            invitacion.setEstado(Invitacion.EstadoInvitacion.EXPIRADA);
            invitacionRepository.save(invitacion);
            throw new BadRequestException("La invitación ha expirado");
        }

        invitacion.setEstado(Invitacion.EstadoInvitacion.RECHAZADA);
        invitacion.setDestinatario(usuarioRepository.getReferenceById(usuarioId));
        invitacion = invitacionRepository.save(invitacion);
        logService.registrar("Invitacion", invitacion.getId(), LogModificacion.AccionLog.EDITAR, null, null,
                invitacion.getCompeticion() != null ? invitacion.getCompeticion().getId() : null);

        return invitacionMapper.toDetalleDTO(invitacion);
    }

    /// === CONSULTAS POR PENDIENTES, ENVIADAS, COMPETICIÓN === ///

    /**
     * Devuelve las invitaciones pendientes dirigidas al email del usuario indicado.
     *
     * @param usuarioId identificador del usuario destinatario
     * @return lista de invitaciones en estado {@code PENDIENTE}
     * @throws com.compapption.api.exception.ResourceNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public List<InvitacionSimpleDTO> obtenerPendientes(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", usuarioId));
        return invitacionMapper.toSimpleDTOList(
                invitacionRepository.findPendientesByEmail(usuario.getEmail()));
    }

    /**
     * Devuelve todas las invitaciones enviadas por un emisor, independientemente de su estado.
     *
     * @param emisorId identificador del usuario emisor
     * @return lista de invitaciones emitidas por el usuario
     */
    @Transactional(readOnly = true)
    public List<InvitacionSimpleDTO> obtenerEnviadas(Long emisorId) {
        return invitacionMapper.toSimpleDTOList(
                invitacionRepository.findByEmisorId(emisorId));
    }

    /**
     * Devuelve todas las invitaciones asociadas a una competición concreta.
     *
     * @param competicionId identificador de la competición
     * @return lista de invitaciones de la competición
     * @throws com.compapption.api.exception.ResourceNotFoundException si la competición no existe
     */
    @Transactional(readOnly = true)
    public List<InvitacionSimpleDTO> obtenerPorCompeticion(Long competicionId) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return invitacionMapper.toSimpleDTOList(
                invitacionRepository.findByCompeticionId(competicionId));
    }

    /// === CADUCAR INVITACIONES === ///

    /**
     * Marca como {@code EXPIRADA} todas las invitaciones pendientes cuya fecha de expiración
     * sea anterior al momento actual. Pensado para ser invocado por un scheduler periódico.
     *
     * @return número de invitaciones actualizadas
     */
    @Transactional
    public int marcarExpiradas() { return invitacionRepository.marcarExpiradas(LocalDateTime.now());}

    // Helpers

    private void asignarRolEncompeticion(Usuario usuario, Competicion competicion, Rol.RolNombre rolNombre) {
        if (usuarioRolCompeticionRepository.existsByUsuarioIdAndCompeticionIdAndRolNombre(
                usuario.getId(), competicion.getId(), rolNombre)) {
            return;
        }
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(()-> new ResourceNotFoundException("Rol", "nombre", rolNombre.name()));
        UsuarioRolCompeticion urc = UsuarioRolCompeticion.builder()
                .usuario(usuario)
                .competicion(competicion)
                .rol(rol)
                .build();
        usuarioRolCompeticionRepository.save(urc);
    }

    private void asignarManagerEquipo(Usuario usuario, Equipo equipo, Competicion competicion) {
        if (equipoManagerRepository.existsByEquipoIdAndCompeticionIdAndUsuarioId(
                        equipo.getId(), competicion.getId(), usuario.getId())) {
            return;
        }
        EquipoManager manager = EquipoManager.builder()
                .equipo(equipo)
                .competicion(competicion)
                .usuario(usuario)
                .build();
        equipoManagerRepository.save(manager);
    }

    private Jugador obtenerOCrearJugador(Usuario usuario) {
        return jugadorRepository.findByUsuarioId(usuario.getId())
                .orElseGet(()-> {
                    Jugador jugador = Jugador.builder()
                            .nombre(usuario.getNombre()!=null ? usuario.getNombre() : usuario.getUsername())
                            .apellidos(usuario.getApellidos())
                            .usuario(usuario)
                            .build();
                    return jugadorRepository.save(jugador);
                });
    }

    private void agregarJugadorAEquipo(Jugador jugador, Equipo equipo) {
        if (equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(equipo.getId(), jugador.getId())) {
            return;
        }
        EquipoJugador equipoJugador = EquipoJugador.builder()
                .equipo(equipo)
                .jugador(jugador)
                .activo(true)
                .build();
        equipoJugadorRepository.save(equipoJugador);
    }
}