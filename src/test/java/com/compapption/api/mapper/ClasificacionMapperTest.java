package com.compapption.api.mapper;

import com.compapption.api.dto.clasificacionDTO.ClasificacionDetalleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionUpdateDTO;
import com.compapption.api.entity.Clasificacion;
import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Equipo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(ClasificacionMapperImpl.class)
class ClasificacionMapperTest {

    @Autowired
    private ClasificacionMapper mapper;

    // =========================================================
    // toDetalleDTO() — equipoEscudoUrl se propaga directamente desde Equipo.escudoUrl
    // =========================================================

    @Test
    void toDetalleDTO_conEscudoUrl_propagaLaUrl() {
        String escudoUrl = "https://cdn.example.com/escudo/test-fc.png";
        Equipo equipo = Equipo.builder().id(1L).nombre("Test FC").escudoUrl(escudoUrl).build();
        Competicion competicion = Competicion.builder().id(10L).build();

        Clasificacion clasificacion = Clasificacion.builder()
                .equipo(equipo).competicion(competicion)
                .build();

        ClasificacionDetalleDTO dto = mapper.toDetalleDTO(clasificacion);

        assertThat(dto.getEquipoEscudoUrl()).isEqualTo(escudoUrl);
    }

    @Test
    void toDetalleDTO_sinEscudoUrl_equipoEscudoUrlEsNull() {
        Equipo equipo = Equipo.builder().id(1L).nombre("Sin Escudo").escudoUrl(null).build();
        Competicion competicion = Competicion.builder().id(10L).build();

        Clasificacion clasificacion = Clasificacion.builder()
                .equipo(equipo).competicion(competicion)
                .build();

        ClasificacionDetalleDTO dto = mapper.toDetalleDTO(clasificacion);

        assertThat(dto.getEquipoEscudoUrl()).isNull();
    }

    @Test
    void toDetalleDTO_mapeaCamposAnidados() {
        Equipo equipo = Equipo.builder().id(5L).nombre("Atlético Test").build();
        Competicion competicion = Competicion.builder().id(20L).build();

        Clasificacion clasificacion = Clasificacion.builder()
                .equipo(equipo).competicion(competicion)
                .puntos(9).victorias(3).empates(0).derrotas(0)
                .build();

        ClasificacionDetalleDTO dto = mapper.toDetalleDTO(clasificacion);

        assertThat(dto.getEquipoId()).isEqualTo(5L);
        assertThat(dto.getEquipoNombre()).isEqualTo("Atlético Test");
        assertThat(dto.getCompeticionId()).isEqualTo(20L);
        assertThat(dto.getPuntos()).isEqualTo(9);
    }

    // =========================================================
    // updateEntityFromDTO() — merge parcial (NullValuePropertyMappingStrategy.IGNORE)
    // =========================================================

    @Test
    void updateEntityFromDTO_camposNoNulos_actualizaLosValores() {
        Clasificacion entity = Clasificacion.builder()
                .puntos(3).victorias(1).empates(0).derrotas(0).build();

        ClasificacionUpdateDTO dto = ClasificacionUpdateDTO.builder()
                .puntos(10).victorias(3).build();

        mapper.updateEntityFromDTO(dto, entity);

        assertThat(entity.getPuntos()).isEqualTo(10);
        assertThat(entity.getVictorias()).isEqualTo(3);
    }

    @Test
    void updateEntityFromDTO_camposNulos_noSobreescribeEntidad() {
        Clasificacion entity = Clasificacion.builder()
                .puntos(5).victorias(1).golesFavor(8).build();

        // dto con puntos=7, el resto nulo
        ClasificacionUpdateDTO dto = ClasificacionUpdateDTO.builder().puntos(7).build();

        mapper.updateEntityFromDTO(dto, entity);

        assertThat(entity.getPuntos()).isEqualTo(7);
        assertThat(entity.getVictorias()).isEqualTo(1);   // sin cambios
        assertThat(entity.getGolesFavor()).isEqualTo(8);  // sin cambios
    }
}
