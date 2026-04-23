package com.compapption.api.service;

import com.compapption.api.dto.mensaje.ConversacionSimpleDTO;
import com.compapption.api.dto.mensaje.MensajeDTO;
import com.compapption.api.entity.Conversacion;
import com.compapption.api.entity.Mensaje;
import com.compapption.api.entity.Notificacion;
import com.compapption.api.entity.Usuario;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.mapper.MensajeMapper;
import com.compapption.api.repository.ConversacionRepository;
import com.compapption.api.repository.MensajeRepository;
import com.compapption.api.repository.UsuarioRepository;
import com.compapption.api.request.mensaje.ConversacionStartRequest;
import com.compapption.api.request.mensaje.MensajeCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio de mensajería privada 1 a 1 entre usuarios. Gestiona la creación y reutilización
 * de conversaciones (normalizando siempre la pareja de usuarios por id ascendente para evitar
 * duplicados), el listado de la bandeja de entrada con previsualización y contadores de
 * mensajes sin leer, el envío y la marca como leído.
 * <p>
 * Al enviar un mensaje se dispara además una notificación en tiempo real de tipo
 * {@code MENSAJE_RECIBIDO} al otro participante, delegando en {@link NotificacionService}.
 * </p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class MensajeriaService {

    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final MensajeMapper mensajeMapper;
    private final NotificacionService notificacionService;

    /// === BANDEJA DE ENTRADA === ///

    /**
     * Lista todas las conversaciones en las que participa el usuario indicado, ordenadas
     * por la fecha del último mensaje descendente. Cada entrada incluye el otro participante,
     * una previsualización del último mensaje y el contador de mensajes sin leer.
     *
     * @param usuarioId identificador del usuario que consulta
     * @return lista de {@link ConversacionSimpleDTO} para la bandeja de entrada
     */
    @Transactional(readOnly = true)
    public List<ConversacionSimpleDTO> listarConversaciones(Long usuarioId) {
        List<Conversacion> conversaciones =
                conversacionRepository.findAllByUsuarioAIdOrUsuarioBIdOrderByFechaUltimoMensajeDesc(usuarioId);
        return conversaciones.stream()
                .map(c -> toSimpleDTO(c, usuarioId))
                .toList();
    }

    /**
     * Lista de forma paginada los mensajes de una conversación. Valida que el usuario
     * solicitante sea uno de los dos participantes.
     *
     * @param conversacionId identificador de la conversación
     * @param usuarioId      identificador del usuario que consulta
     * @param pageable       configuración de paginación
     * @return página de {@link MensajeDTO}
     * @throws ResourceNotFoundException si la conversación no existe
     * @throws UnauthorizedException     si el usuario no pertenece a la conversación
     */
    @Transactional(readOnly = true)
    public Page<MensajeDTO> listarMensajes(Long conversacionId, Long usuarioId, Pageable pageable) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación", "id", conversacionId));
        verificarPertenencia(conversacion, usuarioId);

        return mensajeRepository.findByConversacionIdOrderByFechaEnvioDesc(conversacionId, pageable)
                .map(mensajeMapper::toDTO);
    }

    /// === CREACIÓN / REUTILIZACIÓN DE CONVERSACIÓN === ///

    /**
     * Busca la conversación existente entre el usuario logado y el destinatario indicado;
     * si no existe, la crea. Los participantes se normalizan siempre de modo que
     * {@code usuarioA} sea el de id menor, evitando conversaciones duplicadas en los dos sentidos.
     *
     * @param usuarioId identificador del usuario que inicia la conversación
     * @param req       datos de la petición, con el id del destinatario
     * @return DTO resumido de la conversación (nueva o reutilizada)
     * @throws BadRequestException       si el destinatario coincide con el propio usuario
     * @throws ResourceNotFoundException si alguno de los usuarios no existe
     */
    @Transactional
    public ConversacionSimpleDTO buscarOCrearConversacion(Long usuarioId, ConversacionStartRequest req) {
        Long destinatarioId = req.getDestinatarioId();
        if (Objects.equals(usuarioId, destinatarioId)) {
            throw new BadRequestException("No puedes iniciar una conversación contigo mismo");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", destinatarioId));

        Usuario a = usuario.getId() < destinatario.getId() ? usuario : destinatario;
        Usuario b = usuario.getId() < destinatario.getId() ? destinatario : usuario;

        Conversacion conversacion = conversacionRepository.findByUsuarioAAndUsuarioB(a, b)
                .orElseGet(() -> conversacionRepository.save(
                        Conversacion.builder()
                                .usuarioA(a)
                                .usuarioB(b)
                                .build()));

        return toSimpleDTO(conversacion, usuarioId);
    }

    /// === ENVÍO DE MENSAJE === ///

    /**
     * Envía un mensaje dentro de una conversación existente. Actualiza la fecha del último
     * mensaje de la conversación y dispara una notificación {@code MENSAJE_RECIBIDO} al
     * otro participante.
     *
     * @param conversacionId identificador de la conversación
     * @param autorId        identificador del autor del mensaje
     * @param req            datos del mensaje (contenido)
     * @return DTO del mensaje creado
     * @throws ResourceNotFoundException si la conversación o el autor no existen
     * @throws UnauthorizedException     si el autor no pertenece a la conversación
     */
    @Transactional
    public MensajeDTO enviarMensaje(Long conversacionId, Long autorId, MensajeCreateRequest req) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación", "id", conversacionId));
        verificarPertenencia(conversacion, autorId);

        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", autorId));

        Mensaje mensaje = mensajeRepository.save(Mensaje.builder()
                .conversacion(conversacion)
                .autor(autor)
                .contenido(req.getContenido())
                .build());

        conversacion.setFechaUltimoMensaje(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        // Notificar al otro participante
        Long otroUsuarioId = obtenerOtroUsuarioId(conversacion, autorId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("conversacionId", conversacion.getId());
        payload.put("autorUsername", autor.getUsername());
        notificacionService.crear(otroUsuarioId, Notificacion.TipoNotificacion.MENSAJE_RECIBIDO, payload);

        return mensajeMapper.toDTO(mensaje);
    }

    /// === LECTURA === ///

    /**
     * Marca como leídos todos los mensajes de la conversación cuyo autor sea distinto
     * del usuario indicado. Verifica previamente la pertenencia del usuario a la conversación.
     *
     * @param conversacionId identificador de la conversación
     * @param usuarioId      identificador del usuario que lee
     * @return número de mensajes actualizados
     * @throws ResourceNotFoundException si la conversación no existe
     * @throws UnauthorizedException     si el usuario no pertenece a la conversación
     */
    @Transactional
    public int marcarComoLeido(Long conversacionId, Long usuarioId) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación", "id", conversacionId));
        verificarPertenencia(conversacion, usuarioId);

        return mensajeRepository.marcarComoLeidos(conversacionId, usuarioId, LocalDateTime.now());
    }

    /// === HELPERS === ///

    private void verificarPertenencia(Conversacion conversacion, Long usuarioId) {
        Long aId = conversacion.getUsuarioA() != null ? conversacion.getUsuarioA().getId() : null;
        Long bId = conversacion.getUsuarioB() != null ? conversacion.getUsuarioB().getId() : null;
        if (!Objects.equals(aId, usuarioId) && !Objects.equals(bId, usuarioId)) {
            throw new UnauthorizedException("No perteneces a esta conversación");
        }
    }

    private Long obtenerOtroUsuarioId(Conversacion conversacion, Long usuarioId) {
        Long aId = conversacion.getUsuarioA().getId();
        Long bId = conversacion.getUsuarioB().getId();
        return Objects.equals(aId, usuarioId) ? bId : aId;
    }

    private ConversacionSimpleDTO toSimpleDTO(Conversacion c, Long usuarioId) {
        Usuario otro = Objects.equals(c.getUsuarioA().getId(), usuarioId)
                ? c.getUsuarioB()
                : c.getUsuarioA();

        List<Mensaje> ultimos = mensajeRepository.findTopByConversacionId(
                c.getId(), PageRequest.of(0, 1));
        String preview = ultimos.isEmpty() ? null : ultimos.get(0).getContenido();

        long unread = mensajeRepository.countByConversacionIdAndAutorIdNotAndLeidoAtIsNull(
                c.getId(), usuarioId);

        return ConversacionSimpleDTO.builder()
                .id(c.getId())
                .otroUsuarioId(otro.getId())
                .otroUsuarioUsername(otro.getUsername())
                .ultimoMensaje(preview)
                .fechaUltimoMensaje(c.getFechaUltimoMensaje())
                .unreadCount(unread)
                .build();
    }
}
