package com.compapption.api.dto.tipoestadisticaDTO;

import com.compapption.api.entity.TipoEstadistica;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
