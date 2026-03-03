package com.compapption.api.dto.tipoestadisticaDTO;

import com.compapption.api.entity.TipoEstadistica;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con la definición de un tipo de estadística (nombre, descripción, tipo de valor y orden),
 * utilizado en la gestión de estadísticas por deporte y en DeporteDetalleDTO.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstadisticaDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private TipoEstadistica.TipoValor tipoValor;
    private Integer orden;
}
