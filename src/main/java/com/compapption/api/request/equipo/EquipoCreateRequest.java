package com.compapption.api.request.equipo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para crear un equipo. Contiene nombre, descripción y URL del escudo (imagen
 * alojada externamente, p.ej. Cloudinary).
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoCreateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    private String descripcion;

    @Size(max = 512, message = "La URL del escudo no puede exceder 512 caracteres")
    private String escudoUrl;

    /**
     * Visibilidad del equipo. {@code true} (por defecto) lo hace visible en el
     * buscador al inscribir equipos en una competición. {@code false} lo oculta;
     * el backend genera un código de invitación que el creador puede compartir
     * para que admins de competición puedan invitarlo.
     */
    private Boolean publico;
}
