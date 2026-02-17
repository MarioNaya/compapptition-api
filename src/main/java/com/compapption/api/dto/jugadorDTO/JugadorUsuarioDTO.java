package com.compapption.api.dto.jugadorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JugadorUsuarioDTO {

    private Long id;
    private Long usuarioid;
    private String usuarioUsername;
    private LocalDateTime fechaCreacion;
}
