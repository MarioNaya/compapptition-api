package com.compapption.api.request.competicion;

import com.compapption.api.entity.ConfiguracionCompeticion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Petición para crear una competición. Contiene nombre, deporte, configuración de formato y opciones de visibilidad.
 *
 * @author Mario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompeticionCreateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El deporte es obligatorio")
    private Long deporteId;

    private Integer temporadaActual;

    private boolean publica;
    private boolean inscripcionAbierta;
    private boolean estadisticasActivas;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private ConfiguracionRequest configuracion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfiguracionRequest {

        private Integer puntosVictoria;
        private Integer puntosEmpate;
        private Integer puntosDerrota;
        private ConfiguracionCompeticion.FormatoCompeticion formato;
        private Integer numEquiposPlayoff;
        private Integer partidosEliminatoria;
    }

}
