package com.compapption.api.dto.mensaje;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO resumido de una conversación 1 a 1, pensado para la bandeja de entrada.
 * Expone los datos del otro participante desde la perspectiva del usuario logado,
 * el último mensaje (previsualización), la fecha del último mensaje y el número de
 * mensajes sin leer.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionSimpleDTO {
    private Long id;
    private Long otroUsuarioId;
    private String otroUsuarioUsername;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private long unreadCount;
}
