package com.compapption.api.dto.CompeticionDTO;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompeticionDetalleDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Long deporteId;
    private String deporteNombre;
    private Long creadorId;
    private String creadorUsername;
    private Boolean publica;
    private Boolean inscripcionAbierta;
    private Boolean estadisticasActivas;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Competicion.EstadoCompeticion estado;
    private LocalDateTime fechaCreacion;
    private ConfiguracionDTO configuracion;
    private Integer numEquipos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfiguracionDTO {
        private Integer puntosVictoria;
        private Integer puntosEmpate;
        private Integer puntosDerrota;
        private ConfiguracionCompeticion.FormatoCompeticion formato;
        private Integer numEquiposPlayoff;
        private Integer partidosEliminatoria;
    }

}
