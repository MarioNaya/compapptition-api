package com.compapption.api.service;

import com.compapption.api.dto.notificacion.NotificacionDTO;
import com.compapption.api.entity.Notificacion;
import com.compapption.api.entity.Usuario;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.mapper.NotificacionMapper;
import com.compapption.api.repository.NotificacionRepository;
import com.compapption.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio de notificaciones persistentes y entrega en tiempo real vía Server-Sent Events.
 * <p>
 * Gestiona dos canales acoplados:
 * <ul>
 *   <li><b>Persistencia</b> — cada notificación se guarda en la tabla {@code notificacion}
 *       para que el usuario pueda recuperarla aunque no estuviera conectado.</li>
 *   <li><b>SSE</b> — si el usuario tiene un {@link SseEmitter} activo (p.ej. una pestaña
 *       abierta del frontend), se le entrega la notificación al momento.</li>
 * </ul>
 * Los emitters se almacenan en un mapa concurrente ({@code emitters}) indexado por
 * id de usuario; cada usuario puede tener varios emitters simultáneos (varias pestañas).
 * </p>
 *
 * <p>Se utiliza {@link SseEmitter} (Servlet/MVC) en lugar de {@code Flux} para evitar
 * arrastrar WebFlux en un proyecto Spring MVC; el tipo está disponible en
 * {@code spring-web} y se integra de forma natural en Spring MVC.</p>
 *
 * @author Mario
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    /** Timeout del emisor SSE: 30 minutos. Los clientes reconectan automáticamente. */
    private static final long SSE_TIMEOUT_MILLIS = 30L * 60 * 1000;

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionMapper notificacionMapper;
    private final ObjectMapper objectMapper;

    /** Mapa concurrente de emitters activos indexado por id de usuario. */
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /// === STREAMING SSE === ///

    /**
     * Registra un nuevo {@link SseEmitter} para el usuario indicado y lo almacena en el
     * mapa {@code emitters}. Configura callbacks de {@code complete/timeout/error} para
     * eliminarlo automáticamente del mapa cuando la conexión termine.
     *
     * @param usuarioId identificador del usuario que se suscribe al stream
     * @return emisor SSE configurado con timeout de 30 minutos
     */
    public SseEmitter subscribe(Long usuarioId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);

        CopyOnWriteArrayList<SseEmitter> list = emitters.computeIfAbsent(
                usuarioId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        emitter.onCompletion(() -> removeEmitter(usuarioId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(usuarioId, emitter);
        });
        emitter.onError(e -> removeEmitter(usuarioId, emitter));

        // Handshake inicial para que el cliente confirme la conexión
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            log.debug("No se pudo enviar evento 'connected' a usuario {}: {}", usuarioId, e.getMessage());
            removeEmitter(usuarioId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Long usuarioId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(usuarioId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(usuarioId);
            }
        }
    }

    /// === CREACIÓN Y ENTREGA === ///

    /**
     * Crea una notificación persistente para el destinatario y la entrega inmediatamente
     * a sus {@link SseEmitter} activos. El payload se serializa a JSON para almacenarse y
     * se transporta ya deserializado al cliente.
     * <p>
     * Si el usuario no está conectado por SSE la notificación queda únicamente persistida;
     * podrá recuperarla al llamar a {@link #listar}.
     * </p>
     *
     * @param destinatarioId identificador del usuario destinatario
     * @param tipo           tipo funcional de la notificación
     * @param payload        datos asociados al evento (ids y nombres relacionados); puede ser {@code null}
     * @return DTO de la notificación creada
     * @throws ResourceNotFoundException si el destinatario no existe
     */
    @Transactional
    public NotificacionDTO crear(Long destinatarioId, Notificacion.TipoNotificacion tipo,
                                 Map<String, Object> payload) {
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", destinatarioId));

        String payloadJson = serializePayload(payload);

        Notificacion notificacion = Notificacion.builder()
                .destinatario(destinatario)
                .tipo(tipo)
                .payloadJson(payloadJson)
                .leida(false)
                .build();

        notificacion = notificacionRepository.save(notificacion);
        NotificacionDTO dto = notificacionMapper.toDTO(notificacion);

        enviarPorSse(destinatarioId, dto);

        return dto;
    }

    private String serializePayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("No se pudo serializar payload: {}", e.getMessage());
            return null;
        }
    }

    private void enviarPorSse(Long usuarioId, NotificacionDTO dto) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(usuarioId);
        if (list == null || list.isEmpty()) return;

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(dto));
            } catch (Exception e) {
                log.debug("Error enviando SSE a usuario {}: {}", usuarioId, e.getMessage());
                removeEmitter(usuarioId, emitter);
            }
        }
    }

    /// === CONSULTAS === ///

    /**
     * Lista de forma paginada las notificaciones del usuario, con filtro opcional por
     * estado de lectura.
     *
     * @param usuarioId identificador del usuario destinatario
     * @param pageable  configuración de paginación
     * @param leida     si es {@code null} devuelve todas; si no, filtra por ese valor
     * @return página de {@link NotificacionDTO}
     */
    @Transactional(readOnly = true)
    public Page<NotificacionDTO> listar(Long usuarioId, Pageable pageable, Boolean leida) {
        Page<Notificacion> page = (leida == null)
                ? notificacionRepository.findByDestinatarioIdOrderByFechaCreacionDesc(usuarioId, pageable)
                : notificacionRepository.findByDestinatarioIdAndLeidaOrderByFechaCreacionDesc(
                        usuarioId, leida, pageable);
        return page.map(notificacionMapper::toDTO);
    }

    /**
     * Marca una notificación concreta como leída. Verifica que pertenezca al usuario
     * solicitante antes de actualizar.
     *
     * @param id        identificador de la notificación
     * @param usuarioId identificador del usuario que solicita la operación
     * @throws ResourceNotFoundException si la notificación no existe
     * @throws UnauthorizedException     si la notificación no pertenece al usuario
     */
    @Transactional
    public void marcarLeida(Long id, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", id));

        if (!notificacion.getDestinatario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No puedes marcar notificaciones ajenas");
        }

        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }
    }

    /**
     * Marca como leídas todas las notificaciones pendientes del usuario.
     *
     * @param usuarioId identificador del usuario destinatario
     * @return número de notificaciones actualizadas
     */
    @Transactional
    public int marcarTodasLeidas(Long usuarioId) {
        return notificacionRepository.marcarTodasLeidas(usuarioId);
    }
}
