package com.compapption.api.dto.deporteDTO;

import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeporteDetalleDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Byte[] icono;
    private Boolean activo;
    private List<TipoEstadisticaDTO> tipoEstadisticas;
}
