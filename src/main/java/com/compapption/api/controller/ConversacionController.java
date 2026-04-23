package com.compapption.api.controller;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.mensaje.ConversacionSimpleDTO;
import com.compapption.api.dto.mensaje.MensajeDTO;
import com.compapption.api.request.mensaje.ConversacionStartRequest;
import com.compapption.api.request.mensaje.MensajeCreateRequest;
import com.compapption.api.service.MensajeriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la mensajería privada 1 a 1. Expone endpoints bajo la ruta
 * base {@code /conversaciones}. Todos los endpoints requieren autenticación; el usuario
 * logado se extrae del {@link CustomUserDetails} y se usa como identificador de autor
 * o participante.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/conversaciones")
@RequiredArgsConstructor
public class ConversacionController {

    private final MensajeriaService mensajeriaService;

    /**
     * GET /conversaciones — lista las conversaciones del usuario logado, ordenadas por
     * fecha del último mensaje descendente.
     *
     * @param userDetails datos del usuario autenticado
     * @return lista de {@link ConversacionSimpleDTO}
     */
    @GetMapping
    public ResponseEntity<List<ConversacionSimpleDTO>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(mensajeriaService.listarConversaciones(userDetails.getId()));
    }

    /**
     * GET /conversaciones/{id}/mensajes — lista de forma paginada los mensajes de una
     * conversación. Solo puede consultarlos un participante de la misma.
     *
     * @param id          identificador de la conversación
     * @param pageable    parámetros de paginación (por defecto 50 por página)
     * @param userDetails datos del usuario autenticado
     * @return página de {@link MensajeDTO}
     */
    @GetMapping("/{id}/mensajes")
    public ResponseEntity<Page<MensajeDTO>> listarMensajes(
            @PathVariable Long id,
            @PageableDefault(size = 50) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(mensajeriaService.listarMensajes(id, userDetails.getId(), pageable));
    }

    /**
     * POST /conversaciones — busca o crea la conversación entre el usuario logado y el
     * destinatario indicado en el cuerpo.
     *
     * @param request     datos con el id del destinatario
     * @param userDetails datos del usuario autenticado
     * @return DTO resumido de la conversación
     */
    @PostMapping
    public ResponseEntity<ConversacionSimpleDTO> buscarOCrear(
            @Valid @RequestBody ConversacionStartRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConversacionSimpleDTO dto = mensajeriaService.buscarOCrearConversacion(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * POST /conversaciones/{id}/mensajes — envía un mensaje en una conversación.
     *
     * @param id          identificador de la conversación
     * @param request     cuerpo con el contenido del mensaje
     * @param userDetails datos del usuario autenticado (autor)
     * @return DTO del mensaje creado
     */
    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeDTO> enviar(
            @PathVariable Long id,
            @Valid @RequestBody MensajeCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MensajeDTO dto = mensajeriaService.enviarMensaje(id, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * PATCH /conversaciones/{id}/leer — marca como leídos todos los mensajes de la
     * conversación cuyo autor no sea el usuario logado.
     *
     * @param id          identificador de la conversación
     * @param userDetails datos del usuario autenticado
     * @return mapa con el número de mensajes marcados
     */
    @PatchMapping("/{id}/leer")
    public ResponseEntity<Map<String, Integer>> marcarLeido(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        int actualizados = mensajeriaService.marcarComoLeido(id, userDetails.getId());
        return ResponseEntity.ok(Map.of("actualizados", actualizados));
    }
}
