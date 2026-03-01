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
import java.util.List;
import java.util.UUID;

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

    public static final List<String> ROLES_VALIDOS = List.of("ADMIN_COMPETICION", "MANAGER_EQUIPO", "JUGADOR");

    /// === CREACIÓN DE INVITACIÓN === ///

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

        return invitacionMapper.toDetalleDTO(invitacion);
    }

    /// === ACEPTAR Y RECHAZAR INVITACIÓN === ///

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

        return invitacionMapper.toDetalleDTO(invitacion);
    }

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

    @Transactional(readOnly = true)
    public List<InvitacionSimpleDTO> obtenerPendientes(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", usuarioId));
        return invitacionMapper.toSimpleDTOList(
                invitacionRepository.findPendientesByEmail(usuario.getEmail()));
    }

    @Transactional(readOnly = true)
    public List<InvitacionSimpleDTO> obtenerEnviadas(Long emisorId) {
        return invitacionMapper.toSimpleDTOList(
                invitacionRepository.findByEmisorId(emisorId));
    }

    @Transactional(readOnly = true)
    public List<InvitacionSimpleDTO> obtenerPorCompeticion(Long competicionId) {
        if (!competicionRepository.existsById(competicionId)) {
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return invitacionMapper.toSimpleDTOList(
                invitacionRepository.findByCompeticionId(competicionId));
    }

    /// === CADUCAR INVITACIONES === ///

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