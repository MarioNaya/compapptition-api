package com.compapption.api.request.equipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para actualizar un equipo existente. Permite modificar nombre, descripción y escudo del equipo.
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

    private byte[] escudo;
}
