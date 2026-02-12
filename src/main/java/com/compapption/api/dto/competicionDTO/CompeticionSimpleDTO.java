package com.compapption.api.dto.competicionDTO;

import com.compapption.api.entity.Competicion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompeticionSimpleDTO {

    private long id;
    private String nombre;
    private String deporteNombre;
    private Competicion.EstadoCompeticion estado;

}
