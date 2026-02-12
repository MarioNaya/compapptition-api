package com.compapption.api.dto.competicionDTO;

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

    private long id;
    private String nombre;
    private String descripcion;
    private long deporteId;
    private String deporteNombre;
    private long creadorId;
    private String creadorUsername;
    private boolean publica;
    private boolean inscripcionAbierta;
    private boolean estadisticasActivas;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Competicion.EstadoCompeticion estado;
    private LocalDateTime fechaCreacion;
    private ConfiguracionDTO configuracion;
    private int numEquipos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfiguracionDTO {
        private int puntosVictoria;
        private int puntosEmpate;
        private int puntosDerrota;
        private ConfiguracionCompeticion.FormatoCompeticion formato;
        private int numEquiposPlayoff;
        private int partidosEliminatoria;
    }

}
