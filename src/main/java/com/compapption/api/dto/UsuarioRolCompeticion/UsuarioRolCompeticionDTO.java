package com.compapption.api.dto.UsuarioRolCompeticion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRolCompeticionDTO {
    Long usuarioId;
    String username;
    String email;
    String rolNombre;
    LocalDateTime fechaAsignacion;
}
