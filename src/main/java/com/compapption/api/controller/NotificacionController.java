package com.compapption.api.controller;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.notificacion.NotificacionDTO;
import com.compapption.api.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * Controlador REST para las notificaciones del usuario. Expone endpoints bajo la ruta
 * base {@code /notificaciones}. Todos los endpoints requieren autenticación; la fuente
 * de identificación del usuario es {@link CustomUserDetails}.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * GET /notificaciones — lista paginada de las notificaciones del usuario logado, con
     * filtro opcional por estado de lectura.
     *
     * @param leida       filtro opcional: {@code true}, {@code false} o ausente para todas
     * @param pageable    parámetros de paginación (por defecto 20 por página)
     * @param userDetails datos del usuario autenticado
     * @return página de {@link NotificacionDTO}
     */
    @GetMapping
    public ResponseEntity<Page<NotificacionDTO>> listar(
            @RequestParam(required = false) Boolean leida,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificacionService.listar(userDetails.getId(), pageable, leida));
    }

    /**
     * PATCH /notificaciones/{id}/leer — marca una notificación como leída. Solo se puede
     * marcar una notificación propia.
     *
     * @param id          identificador de la notificación
     * @param userDetails datos del usuario autenticado
     * @return respuesta vacía 204 No Content
     */
    @PatchMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificacionService.marcarLeida(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /notificaciones/leer-todas — marca como leídas todas las notificaciones del
     * usuario logado.
     *
     * @param userDetails datos del usuario autenticado
     * @return mapa con el número de notificaciones actualizadas
     */
    @PatchMapping("/leer-todas")
    public ResponseEntity<Map<String, Integer>> marcarTodasLeidas(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        int actualizadas = notificacionService.marcarTodasLeidas(userDetails.getId());
        return ResponseEntity.ok(Map.of("actualizadas", actualizadas));
    }

    /**
     * GET /notificaciones/stream — abre un stream Server-Sent Events (SSE) para recibir
     * notificaciones en tiempo real. El emisor está configurado con un timeout de 30 minutos
     * y el cliente debe reconectar al caducar.
     *
     * @param userDetails datos del usuario autenticado
     * @return {@link SseEmitter} asociado al usuario logado
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificacionService.subscribe(userDetails.getId());
    }
}
