package com.compapption.api.request.tipoestadistica;

import com.compapption.api.entity.TipoEstadistica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para crear o actualizar un tipo de estadística. Contiene nombre, descripción, tipo de valor y orden de visualización.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstadisticaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    private String descripcion;

    private TipoEstadistica.TipoValor tipoValor;

    private Integer orden;
}
