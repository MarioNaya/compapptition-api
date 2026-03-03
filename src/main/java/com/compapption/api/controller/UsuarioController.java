package com.compapption.api.controller;

import com.compapption.api.dto.usuario.UsuarioDTO;
import com.compapption.api.request.usuario.UsuarioUpdateRequest;
import com.compapption.api.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de usuarios. Expone endpoints bajo la ruta base /usuarios.
 * Gestiona la consulta, actualización, cambio de contraseña y desactivación de cuentas de usuario.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /usuarios/{id} — obtiene los datos de un usuario por su identificador.
     *
     * @param id identificador único del usuario
     * @return ResponseEntity con el UsuarioDTO del usuario solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    /**
     * GET /usuarios/buscar — busca un usuario por su nombre de usuario (username).
     *
     * @param username nombre de usuario a buscar
     * @return ResponseEntity con el UsuarioDTO del usuario encontrado
     */
    @GetMapping("/buscar")
    public ResponseEntity<UsuarioDTO> obtenerPorUsername(@RequestParam String username) {
        return ResponseEntity.ok(usuarioService.obtenerPorUsername(username));
    }

    /**
     * PUT /usuarios/{id} — actualiza los datos del perfil de un usuario.
     *
     * @param id identificador único del usuario a actualizar
     * @param request cuerpo de la petición con los nuevos datos del perfil
     * @return ResponseEntity con el UsuarioDTO actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    /**
     * POST /usuarios/{id}/cambiar-password — cambia la contraseña del usuario autenticado.
     * Requiere que se proporcione la contraseña actual para verificar la identidad.
     *
     * @param id identificador único del usuario
     * @param request cuerpo con la contraseña actual y la nueva contraseña
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @PostMapping("/{id}/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(id, request.getPasswordActual(), request.getPasswordNuevo());
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /usuarios/{id} — desactiva (baja lógica) la cuenta de un usuario.
     *
     * @param id identificador único del usuario a desactivar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class CambiarPasswordRequest {
        @NotBlank private String passwordActual;
        @NotBlank private String passwordNuevo;
    }
}
