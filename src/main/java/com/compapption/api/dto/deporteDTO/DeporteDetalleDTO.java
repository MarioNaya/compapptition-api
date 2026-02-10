package com.compapption.api.dto.deporteDTO;

import com.compapption.api.entity.TipoEstadistica;
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
    private byte[] icono;
    private Boolean activo;
    private List<TipoEstadistica> tipoEstadisticas;
}
