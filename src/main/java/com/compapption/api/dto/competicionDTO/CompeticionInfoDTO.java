package com.compapption.api.dto.competicionDTO;

import com.compapption.api.entity.Competicion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompeticionInfoDTO {

    private long id;
    private String nombre;
    private String descripcion;
    private String deporteNombre;
    private String creadorUsername;
    private boolean publica;
    private Competicion.EstadoCompeticion estado;
    private int numEquipos;
    private LocalDate fechaInicio;
}
