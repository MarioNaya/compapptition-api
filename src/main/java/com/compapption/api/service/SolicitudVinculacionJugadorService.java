package com.compapption.api.service;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.jugadorDTO.SolicitudVinculacionJugadorDTO;
import com.compapption.api.entity.*;
import com.compapption.api.entity.SolicitudVinculacionJugador.Estado;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.repository.*;
import com.compapption.api.request.jugador.SolicitudVinculacionAdminRequest;
import com.compapption.api.request.jugador.SolicitudVinculacionAutoRequest;
import com.compapption.api.service.log.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que gobierna el ciclo de vida de las {@link SolicitudVinculacionJugador},
 * con doble validación: una parte inicia la solicitud (admin/manager o el propio
 * usuario candidato) y la otra debe aceptar o rechazar antes de que el efecto
 * (vinculación {@code Jugador.usuario = Usuario}) se aplique.
 *
 * <p>La autorización de cada acción se valida aquí mismo, no solo en el controller,
 * porque el aprobador legítimo depende del estado actual de la solicitud.</p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class SolicitudVinculacionJugadorService {

    private static final List<Estado> ESTADOS_ABIERTOS =
            List.of(Estado.PENDIENTE_USUARIO, Estado.PENDIENTE_ADMIN);

    private static final long DIAS_EXPIRACION = 7L;

    private final SolicitudVinculacionJugadorRepository repo;
    private final JugadorRepository jugadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;
    private final RbacService rbacService;
    private final NotificacionService notificacionService;
    private final LogService logService;

    // ============================== INICIO ==============================

    /**
     * Lado admin/manager: propone vincular el jugador con la cuenta de un usuario.
     * La solicitud queda en {@code PENDIENTE_USUARIO} hasta que el usuario destino
     * acepte o rechace.
     */
    @Transactional
    public SolicitudVinculacionJugadorDTO iniciarComoAdmin(
            Long jugadorId, SolicitudVinculacionAdminRequest request, Long iniciadorId) {

        Jugador jugador = cargarJugadorVinculable(jugadorId);
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getUsuarioId()));
        Equipo equipo = cargarEquipoConJugador(request.getEquipoId(), jugador);
        Usuario iniciador = usuarioRepository.findById(iniciadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", iniciadorId));

        rechazarSiYaTieneCuenta(usuario);
        rechazarSiDuplicada(jugadorId, usuario.getId(), equipo.getId());

        SolicitudVinculacionJugador solicitud = SolicitudVinculacionJugador.builder()
                .jugador(jugador)
                .usuario(usuario)
                .iniciador(iniciador)
                .equipo(equipo)
                .estado(Estado.PENDIENTE_USUARIO)
                .fechaExpiracion(LocalDateTime.now().plusDays(DIAS_EXPIRACION))
                .build();
        solicitud = repo.save(solicitud);
        logService.registrar("SolicitudVinculacionJugador", solicitud.getId(),
                LogModificacion.AccionLog.CREAR, null, null, null);

        notificarSolicitudCreada(solicitud, usuario.getId());
        return toDTO(solicitud);
    }

    /**
     * Lado usuario: el propio candidato reclama el perfil del jugador. Queda en
     * {@code PENDIENTE_ADMIN} hasta que un admin/manager del equipo apruebe.
     */
    @Transactional
    public SolicitudVinculacionJugadorDTO iniciarComoUsuario(
            Long jugadorId, SolicitudVinculacionAutoRequest request, Long usuarioId) {

        Jugador jugador = cargarJugadorVinculable(jugadorId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));
        Equipo equipo = cargarEquipoConJugador(request.getEquipoId(), jugador);

        rechazarSiYaTieneCuenta(usuario);
        rechazarSiDuplicada(jugadorId, usuario.getId(), equipo.getId());

        SolicitudVinculacionJugador solicitud = SolicitudVinculacionJugador.builder()
                .jugador(jugador)
                .usuario(usuario)
                .iniciador(usuario)
                .equipo(equipo)
                .estado(Estado.PENDIENTE_ADMIN)
                .fechaExpiracion(LocalDateTime.now().plusDays(DIAS_EXPIRACION))
                .build();
        solicitud = repo.save(solicitud);
        logService.registrar("SolicitudVinculacionJugador", solicitud.getId(),
                LogModificacion.AccionLog.CREAR, null, null, null);

        // Notificar a los aprobadores del lado admin: creador del equipo y managers en
        // alguna competición. Mantener la lista simple — duplicados se descartan vía Set.
        java.util.Set<Long> destinatarios = new java.util.HashSet<>();
        if (equipo.getCreador() != null) destinatarios.add(equipo.getCreador().getId());
        equipo.getManagers().forEach(m -> {
            if (m.getUsuario() != null) destinatarios.add(m.getUsuario().getId());
        });
        destinatarios.remove(usuarioId); // no a uno mismo
        for (Long id : destinatarios) {
            notificarSolicitudCreada(solicitud, id);
        }
        return toDTO(solicitud);
    }

    // ============================== ACEPTAR / RECHAZAR ==============================

    @Transactional
    public SolicitudVinculacionJugadorDTO aceptar(Long solicitudId, Authentication auth) {
        SolicitudVinculacionJugador solicitud = cargarSolicitudAbierta(solicitudId);
        verificarAprobador(solicitud, auth);

        Jugador jugador = solicitud.getJugador();
        Usuario usuario = solicitud.getUsuario();
        if (jugador.getUsuario() != null) {
            throw new BadRequestException("El jugador ya está vinculado a un usuario");
        }
        if (jugadorRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new BadRequestException("El usuario ya tiene un perfil de jugador");
        }
        jugador.setUsuario(usuario);
        jugadorRepository.save(jugador);

        solicitud.setEstado(Estado.ACEPTADA);
        solicitud.setFechaResolucion(LocalDateTime.now());
        solicitud = repo.save(solicitud);
        logService.registrar("SolicitudVinculacionJugador", solicitud.getId(),
                LogModificacion.AccionLog.EDITAR, null, null, null);

        notificarResolucion(solicitud, true);
        return toDTO(solicitud);
    }

    @Transactional
    public SolicitudVinculacionJugadorDTO rechazar(Long solicitudId, Authentication auth) {
        SolicitudVinculacionJugador solicitud = cargarSolicitudAbierta(solicitudId);
        verificarAprobador(solicitud, auth);

        solicitud.setEstado(Estado.RECHAZADA);
        solicitud.setFechaResolucion(LocalDateTime.now());
        solicitud = repo.save(solicitud);
        logService.registrar("SolicitudVinculacionJugador", solicitud.getId(),
                LogModificacion.AccionLog.EDITAR, null, null, null);

        notificarResolucion(solicitud, false);
        return toDTO(solicitud);
    }

    // ============================== CONSULTAS ==============================

    /**
     * Devuelve las solicitudes que requieren acción del usuario logado: si está en
     * {@code PENDIENTE_USUARIO} y él es el destinatario, o si está en
     * {@code PENDIENTE_ADMIN} y él es admin/manager de un equipo asociado.
     */
    @Transactional(readOnly = true)
    public List<SolicitudVinculacionJugadorDTO> pendientesParaUsuario(Long usuarioId) {
        List<SolicitudVinculacionJugador> resultado = new java.util.ArrayList<>(
                repo.findPendientesPorUsuario(usuarioId));

        // Equipos donde el usuario tiene legitimidad admin/manager: creador, manager
        // o admin de alguna de sus competiciones. Para cada solicitud PENDIENTE_ADMIN
        // se evalúa por equipo.
        // Optimización: en lugar de cargar todas las solicitudes globales filtramos
        // por los equipos del usuario gestionables — pero el coste de esa lista
        // tampoco es trivial; en este punto del proyecto basta con un filtro post-fetch.
        List<SolicitudVinculacionJugador> pendientesAdmin = repo.findAll().stream()
                .filter(s -> s.getEstado() == Estado.PENDIENTE_ADMIN)
                .filter(s -> rbacService.puedeGestionarPlantillaParaUsuario(s.getEquipo().getId(), usuarioId))
                .toList();
        resultado.addAll(pendientesAdmin);
        return resultado.stream().map(this::toDTO).toList();
    }

    @Transactional
    public int marcarExpiradas() {
        return repo.marcarExpiradas(LocalDateTime.now());
    }

    // ============================== HELPERS ==============================

    private Jugador cargarJugadorVinculable(Long jugadorId) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador", "id", jugadorId));
        if (jugador.getUsuario() != null) {
            throw new BadRequestException("El jugador ya está vinculado a una cuenta de usuario");
        }
        return jugador;
    }

    private Equipo cargarEquipoConJugador(Long equipoId, Jugador jugador) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo", "id", equipoId));
        if (!equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(equipoId, jugador.getId())) {
            throw new BadRequestException("El jugador no pertenece al equipo indicado");
        }
        return equipo;
    }

    private void rechazarSiYaTieneCuenta(Usuario usuario) {
        if (jugadorRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new BadRequestException("El usuario ya tiene un perfil de jugador");
        }
    }

    private void rechazarSiDuplicada(Long jugadorId, Long usuarioId, Long equipoId) {
        if (repo.existsByJugadorIdAndUsuarioIdAndEquipoIdAndEstadoIn(
                jugadorId, usuarioId, equipoId, ESTADOS_ABIERTOS)) {
            throw new BadRequestException("Ya existe una solicitud de vinculación abierta para esta combinación");
        }
    }

    private SolicitudVinculacionJugador cargarSolicitudAbierta(Long id) {
        SolicitudVinculacionJugador s = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", id));
        if (s.getEstado() == Estado.ACEPTADA || s.getEstado() == Estado.RECHAZADA
                || s.getEstado() == Estado.EXPIRADA) {
            throw new BadRequestException("La solicitud ya está resuelta (estado " + s.getEstado() + ")");
        }
        if (s.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            s.setEstado(Estado.EXPIRADA);
            repo.save(s);
            throw new BadRequestException("La solicitud ha expirado");
        }
        return s;
    }

    private void verificarAprobador(SolicitudVinculacionJugador s, Authentication auth) {
        Long callerId = extractUserId(auth);
        if (callerId == null) throw new UnauthorizedException("Sin autenticación");
        if (rbacService.isAdminSistema(auth)) return;

        switch (s.getEstado()) {
            case PENDIENTE_USUARIO -> {
                if (!s.getUsuario().getId().equals(callerId)) {
                    throw new UnauthorizedException("Solo el usuario candidato puede resolver esta solicitud");
                }
            }
            case PENDIENTE_ADMIN -> {
                if (!rbacService.puedeGestionarPlantilla(s.getEquipo().getId(), auth)) {
                    throw new UnauthorizedException("No tienes permisos sobre el equipo de esta solicitud");
                }
            }
            default -> throw new BadRequestException("Estado no resoluble: " + s.getEstado());
        }
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails u)) return null;
        return u.getId();
    }

    private void notificarSolicitudCreada(SolicitudVinculacionJugador s, Long destinatarioId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("solicitudId", s.getId());
        payload.put("jugadorNombre", s.getJugador().getNombre());
        payload.put("equipoNombre", s.getEquipo().getNombre());
        payload.put("estado", s.getEstado().name());
        notificacionService.crear(destinatarioId,
                Notificacion.TipoNotificacion.SOLICITUD_VINCULACION_RECIBIDA, payload);
    }

    private void notificarResolucion(SolicitudVinculacionJugador s, boolean aceptada) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("solicitudId", s.getId());
        payload.put("jugadorNombre", s.getJugador().getNombre());
        payload.put("equipoNombre", s.getEquipo().getNombre());
        payload.put("aceptada", aceptada);
        // Notificar a la otra parte (la que NO acaba de pulsar aceptar/rechazar):
        // si la pendiente era del usuario, notifico al iniciador (admin); si era del
        // admin, notifico al usuario candidato.
        Long destinatarioId;
        if (s.getIniciador().getId().equals(s.getUsuario().getId())) {
            // la inició el usuario → la resolvió el admin → notifico al usuario
            destinatarioId = s.getUsuario().getId();
        } else {
            // la inició el admin → la resolvió el usuario → notifico al iniciador
            destinatarioId = s.getIniciador().getId();
        }
        notificacionService.crear(destinatarioId,
                Notificacion.TipoNotificacion.SOLICITUD_VINCULACION_RESUELTA, payload);
    }

    private SolicitudVinculacionJugadorDTO toDTO(SolicitudVinculacionJugador s) {
        return SolicitudVinculacionJugadorDTO.builder()
                .id(s.getId())
                .jugadorId(s.getJugador().getId())
                .jugadorNombre(s.getJugador().getNombre())
                .jugadorApellidos(s.getJugador().getApellidos())
                .usuarioId(s.getUsuario().getId())
                .usuarioUsername(s.getUsuario().getUsername())
                .usuarioEmail(s.getUsuario().getEmail())
                .iniciadorId(s.getIniciador().getId())
                .iniciadorUsername(s.getIniciador().getUsername())
                .equipoId(s.getEquipo().getId())
                .equipoNombre(s.getEquipo().getNombre())
                .estado(s.getEstado())
                .fechaCreacion(s.getFechaCreacion())
                .fechaExpiracion(s.getFechaExpiracion())
                .fechaResolucion(s.getFechaResolucion())
                .build();
    }
}
