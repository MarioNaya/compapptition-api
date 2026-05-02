package com.compapption.api.controller;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.usuario.UsuarioDTO;
import com.compapption.api.request.usuario.UsuarioUpdateRequest;
import com.compapption.api.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * GET /usuarios/me — devuelve el perfil del usuario autenticado, incluyendo
     * el flag {@code esAdminSistema}. Sustituye al uso de {@code GET /usuarios/{id}}
     * para auto-consulta y elimina la posibilidad de IDOR contra el propio perfil.
     *
     * @param principal datos del usuario autenticado extraídos del JWT
     * @return ResponseEntity con el UsuarioDTO del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> obtenerMiPerfil(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(principal.getId(), true));
    }

    /**
     * GET /usuarios/{id} — obtiene los datos de un usuario por su identificador.
     * Acceso restringido al propio usuario o al administrador del sistema. El
     * campo {@code esAdminSistema} solo se rellena para el admin de sistema y
     * para el propio usuario.
     *
     * @param id identificador único del usuario
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con el UsuarioDTO del usuario solicitado
     */
    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.esPropietarioOAdminSistema(#id, authentication)")
    public ResponseEntity<UsuarioDTO> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        boolean incluirAdminFlag = principal != null
                && (principal.isEsAdminSistema() || id.equals(principal.getId()));
        return ResponseEntity.ok(usuarioService.obtenerPorId(id, incluirAdminFlag));
    }

    /**
     * GET /usuarios/buscar — busca un usuario por su nombre de usuario (username).
     * Cualquier usuario autenticado puede invocarlo (alimenta selectores de
     * destinatarios de mensajes/invitaciones); el flag {@code esAdminSistema}
     * se omite salvo que el solicitante sea administrador del sistema.
     *
     * @param username nombre de usuario a buscar
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con el UsuarioDTO del usuario encontrado
     */
    @GetMapping("/buscar")
    public ResponseEntity<UsuarioDTO> obtenerPorUsername(
            @RequestParam String username,
            @AuthenticationPrincipal CustomUserDetails principal) {
        boolean incluirAdminFlag = principal != null && principal.isEsAdminSistema();
        return ResponseEntity.ok(usuarioService.obtenerPorUsername(username, incluirAdminFlag));
    }

    /**
     * PUT /usuarios/{id} — actualiza los datos del perfil de un usuario.
     * Solo el propio usuario o un administrador del sistema pueden hacerlo.
     *
     * @param id identificador único del usuario a actualizar
     * @param request cuerpo de la petición con los nuevos datos del perfil
     * @return ResponseEntity con el UsuarioDTO actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.esPropietarioOAdminSistema(#id, authentication)")
    public ResponseEntity<UsuarioDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    /**
     * POST /usuarios/{id}/cambiar-password — cambia la contraseña del usuario.
     * Solo el propio usuario o un administrador del sistema pueden invocarlo.
     * Tras éxito se revocan todos los refresh tokens activos del usuario para
     * forzar nuevo login en el resto de dispositivos.
     *
     * @param id identificador único del usuario
     * @param request cuerpo con la contraseña actual y la nueva contraseña
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @PostMapping("/{id}/cambiar-password")
    @PreAuthorize("@rbacService.esPropietarioOAdminSistema(#id, authentication)")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(id, request.getPasswordActual(), request.getPasswordNuevo());
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /usuarios/{id} — desactiva (baja lógica) la cuenta de un usuario.
     * Solo el propio usuario o un administrador del sistema pueden invocarlo.
     *
     * @param id identificador único del usuario a desactivar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.esPropietarioOAdminSistema(#id, authentication)")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class CambiarPasswordRequest {
        @NotBlank
        private String passwordActual;

        @NotBlank
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                 message = "La contraseña debe contener al menos una letra mayúscula y un dígito")
        private String passwordNuevo;
    }
}
