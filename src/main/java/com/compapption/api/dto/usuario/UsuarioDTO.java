package com.compapption.api.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de un usuario del sistema (identificador, credenciales, nombre y estado),
 * utilizado en los endpoints de gestión de usuarios de UsuarioController.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellidos;
    private Boolean activo;
    private Boolean esAdminSistema;
}
