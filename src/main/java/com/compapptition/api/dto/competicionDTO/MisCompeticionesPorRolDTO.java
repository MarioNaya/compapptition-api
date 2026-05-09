package com.compapptition.api.dto.competicionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agrupa las competiciones del usuario actual por el rol con el que participa
 * (administrador, manager de equipo, árbitro, jugador). Cada lista usa el DTO
 * simple de competición; una misma competición puede aparecer en más de una
 * lista si el usuario tiene varios roles.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MisCompeticionesPorRolDTO {

    private List<CompeticionSimpleDTO> admin;
    private List<CompeticionSimpleDTO> manager;
    private List<CompeticionSimpleDTO> arbitro;
    private List<CompeticionSimpleDTO> jugador;
}
