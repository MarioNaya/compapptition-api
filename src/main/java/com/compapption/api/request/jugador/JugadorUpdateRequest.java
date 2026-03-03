package com.compapption.api.request.jugador;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para actualizar los datos de un jugador existente. Todos los campos son opcionales y solo se aplican los que se envíen.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JugadorUpdateRequest {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    private String apellidos;

    private Integer dorsal;

    @Size(max = 50, message = "La posición no puede exceder 50 caracteres")
    private String posicion;

    private byte[] foto;

    private Long usuarioId;
}
