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
}
