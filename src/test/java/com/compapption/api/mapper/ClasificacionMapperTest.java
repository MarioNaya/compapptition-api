package com.compapption.api.mapper;

import com.compapption.api.dto.clasificacionDTO.ClasificacionDetalleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionUpdateDTO;
import com.compapption.api.entity.Clasificacion;
import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Equipo;
import com.compapption.api.mapper.ClasificacionMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(ClasificacionMapperImpl.class)
class ClasificacionMapperTest {

    @Autowired
    private ClasificacionMapper mapper;

    // =========================================================
    // toDetalleDTO() — conversión byte[] → Base64 en equipoEscudo
    // =========================================================

    @Test
    void toDetalleDTO_conEscudoBytes_convierteABase64() {
        byte[] escudo = {1, 2, 3, 4};
        Equipo equipo = Equipo.builder().id(1L).nombre("Test FC").escudo(escudo).build();
        Competicion competicion = Competicion.builder().id(10L).build();

        Clasificacion clasificacion = Clasificacion.builder()
                .equipo(equipo).competicion(competicion)
                .build();

        ClasificacionDetalleDTO dto = mapper.toDetalleDTO(clasificacion);

        String esperado = Base64.getEncoder().encodeToString(escudo);
        assertThat(dto.getEquipoEscudo()).isEqualTo(esperado);
    }

    @Test
    void toDetalleDTO_sinEscudo_equipoEscudoEsNull() {
        Equipo equipo = Equipo.builder().id(1L).nombre("Sin Escudo").escudo(null).build();
        Competicion competicion = Competicion.builder().id(10L).build();

        Clasificacion clasificacion = Clasificacion.builder()
                .equipo(equipo).competicion(competicion)
                .build();

        ClasificacionDetalleDTO dto = mapper.toDetalleDTO(clasificacion);

        assertThat(dto.getEquipoEscudo()).isNull();
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
