package com.compapption.api.dto.competicionDTO;

import com.compapption.api.entity.Competicion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO con la información general de una competición para listados públicos, incluyendo
 * nombre, deporte, creador, visibilidad, estado y número de equipos inscritos.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompeticionInfoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String deporteNombre;
    private Integer temporadaActual;
    private String creadorUsername;
    private Boolean publica;
    private Competicion.EstadoCompeticion estado;
    private Integer numEquipos;
    private LocalDate fechaInicio;
}
