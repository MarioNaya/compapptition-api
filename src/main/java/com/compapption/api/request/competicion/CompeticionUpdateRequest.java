package com.compapption.api.request.competicion;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompeticionUpdateRequest {

    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    private String descripcion;
    private boolean publica;
    private boolean inscripcionAbierta;
    private boolean estadisticasActivas;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Competicion.EstadoCompeticion estado;

    private ConfiguracionUpdaterequest configuracion;

    public static class ConfiguracionUpdaterequest {
        private int puntosVictoria;
        private int puntosEmpate;
        private int puntosDerrota;
        private ConfiguracionCompeticion.FormatoCompeticion formato;
        private int numEquiposPlayOff;
        private int partidosEliminatoria;
    }
}
