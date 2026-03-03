package com.compapption.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de petición de recuperación de contraseña con el email del usuario, que desencadena
 * el envío de un email con el token de reseteo, utilizado en POST /auth/recuperar-password.
 *
 * @author Mario
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecuperarPasswordRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;
}
