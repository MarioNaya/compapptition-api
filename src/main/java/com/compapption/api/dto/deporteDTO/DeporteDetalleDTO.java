package com.compapption.api.dto.deporteDTO;

import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO con los datos completos de un deporte, incluyendo descripción, icono y la lista
 * de tipos de estadística configurados, devuelto en el endpoint de detalle de deporte.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeporteDetalleDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String iconoUrl;
    private Boolean activo;
    private List<TipoEstadisticaDTO> tipoEstadisticas;
}
