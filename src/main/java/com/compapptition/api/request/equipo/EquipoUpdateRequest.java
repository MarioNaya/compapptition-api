package com.compapptition.api.request.equipo;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para actualizar un equipo existente. Permite modificar nombre, descripción y la URL
 * del escudo (imagen alojada externamente).
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoUpdateRequest {

    private String nombre;

    private String descripcion;

    @Size(max = 512, message = "La URL del escudo no puede exceder 512 caracteres")
    private String escudoUrl;

    /**
     * Cambio de visibilidad. Si pasa de público a privado el backend genera un
     * código de invitación nuevo; si pasa de privado a público el código se
     * limpia (deja de ser válido).
     */
    private Boolean publico;
}
