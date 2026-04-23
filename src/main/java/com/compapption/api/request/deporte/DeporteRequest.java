package com.compapption.api.request.deporte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para crear o actualizar un deporte. Contiene nombre, descripción, URL del icono
 * (imagen alojada externamente) y estado de activación.
 *
 * @author Mario
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeporteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    @Size(max = 512, message = "La URL del icono no puede superar los 512 caracteres")
    private String iconoUrl;

    private Boolean activo;
}
