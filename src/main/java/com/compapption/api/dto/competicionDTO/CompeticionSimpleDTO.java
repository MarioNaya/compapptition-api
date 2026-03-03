package com.compapption.api.dto.competicionDTO;

import com.compapption.api.entity.Competicion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos básicos de una competición (nombre, deporte y estado), utilizado
 * en listados de competiciones del usuario y referencias en otras entidades.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompeticionSimpleDTO {

    private Long id;
    private String nombre;
    private String deporteNombre;
    private Competicion.EstadoCompeticion estado;

}
