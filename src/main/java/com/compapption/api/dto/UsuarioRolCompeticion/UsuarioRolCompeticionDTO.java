package com.compapption.api.dto.UsuarioRolCompeticion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con los datos de la asignación de un rol a un usuario en una competición concreta,
 * utilizado en los endpoints de gestión de roles de CompeticionController.
 *
 * @author Mario
 */
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
