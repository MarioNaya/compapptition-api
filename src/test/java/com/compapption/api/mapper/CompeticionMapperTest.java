package com.compapption.api.mapper;

import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.mapper.CompeticionMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(CompeticionMapperImpl.class)
class CompeticionMapperTest {

    @Autowired
    private CompeticionMapper mapper;

    // =========================================================
    // toDetalleDTO() — numEquipos cuenta solo equipos activos
    // =========================================================

    @Test
    void toDetalleDTO_contaSoloEquiposActivos() {
        CompeticionEquipo activo1 = CompeticionEquipo.builder().activo(true).build();
        CompeticionEquipo activo2 = CompeticionEquipo.builder().activo(true).build();
        CompeticionEquipo inactivo = CompeticionEquipo.builder().activo(false).build();

        Set<CompeticionEquipo> equipos = new HashSet<>();
        equipos.add(activo1);
        equipos.add(activo2);
        equipos.add(inactivo);

        Competicion competicion = competicionBase().equipos(equipos).build();

        CompeticionDetalleDTO dto = mapper.toDetalleDTO(competicion);

        assertThat(dto.getNumEquipos()).isEqualTo(2);
    }

    @Test
    void toDetalleDTO_sinEquipos_numEquiposCero() {
        Competicion competicion = competicionBase().build(); // equipos = empty set por defecto

        CompeticionDetalleDTO dto = mapper.toDetalleDTO(competicion);

        assertThat(dto.getNumEquipos()).isEqualTo(0);
    }

    // =========================================================
    // toConfiguracionDTO() — @Named method, mapea campos completos
    // =========================================================

    @Test
    void toDetalleDTO_configNoNula_mapeaConfiguracionCompleta() {
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .puntosVictoria(3)
                .puntosEmpate(1)
                .puntosDerrota(0)
                .diasEntreJornadas(7)
                .formato(ConfiguracionCompeticion.FormatoCompeticion.LIGA)
                .numEquiposPlayoff(8)
                .partidosEliminatoria(2)
                .build();

        Competicion competicion = competicionBase().configuracion(config).build();

        CompeticionDetalleDTO dto = mapper.toDetalleDTO(competicion);

        assertThat(dto.getConfiguracion()).isNotNull();
        assertThat(dto.getConfiguracion().getPuntosVictoria()).isEqualTo(3);
        assertThat(dto.getConfiguracion().getPuntosEmpate()).isEqualTo(1);
        assertThat(dto.getConfiguracion().getPuntosDerrota()).isEqualTo(0);
        assertThat(dto.getConfiguracion().getDiasEntreJornada()).isEqualTo(7);
        assertThat(dto.getConfiguracion().getFormato())
                .isEqualTo(ConfiguracionCompeticion.FormatoCompeticion.LIGA);
        assertThat(dto.getConfiguracion().getNumEquiposPlayoff()).isEqualTo(8);
        assertThat(dto.getConfiguracion().getPartidosEliminatoria()).isEqualTo(2);
    }

    @Test
    void toDetalleDTO_configNula_configuracionEsNull() {
        Competicion competicion = competicionBase().configuracion(null).build();

        CompeticionDetalleDTO dto = mapper.toDetalleDTO(competicion);

        assertThat(dto.getConfiguracion()).isNull();
    }

    // =========================================================
    // toDetalleDTO() — campos simples de Competicion
    // =========================================================

    @Test
    void toDetalleDTO_mapeaCamposDeporteYCreador() {
        Deporte deporte = Deporte.builder().id(1L).nombre("Fútbol").build();
        Usuario creador = Usuario.builder().id(2L).username("admin").build();

        Competicion competicion = competicionBase()
                .deporte(deporte)
                .creador(creador)
                .build();

        CompeticionDetalleDTO dto = mapper.toDetalleDTO(competicion);

        assertThat(dto.getDeporteId()).isEqualTo(1L);
        assertThat(dto.getDeporteNombre()).isEqualTo("Fútbol");
        assertThat(dto.getCreadorId()).isEqualTo(2L);
        assertThat(dto.getCreadorUsername()).isEqualTo("admin");
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Competicion.CompeticionBuilder competicionBase() {
        return Competicion.builder()
                .id(1L)
                .nombre("Liga Test");
    }
}
