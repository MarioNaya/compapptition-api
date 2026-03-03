package com.compapption.api.dto.competicionDTO;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.ConfiguracionCompeticion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO con los datos completos de una competición, incluyendo configuración de formato,
 * fechas, visibilidad y número de equipos inscritos, devuelto en el endpoint de detalle.
 *
 * @author Mario
 */
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
    private Integer temporadaActual;
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
        private Integer diasEntreJornada;
        private ConfiguracionCompeticion.FormatoCompeticion formato;
        private Integer numEquiposPlayoff;
        private Integer partidosEliminatoria;
    }

}
