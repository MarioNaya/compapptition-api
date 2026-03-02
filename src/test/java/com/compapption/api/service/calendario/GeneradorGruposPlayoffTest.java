package com.compapption.api.service.calendario;

import com.compapption.api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneradorGruposPlayoffTest {

    // GeneradorGruposPlayoff depende de GeneradorLiga — usamos la implementación real
    private final GeneradorGruposPlayoff generador = new GeneradorGruposPlayoff(new GeneradorLiga());

    private Competicion competicion;
    private final LocalDateTime fechaInicio = LocalDateTime.of(2025, 9, 1, 18, 0);

    @BeforeEach
    void setUp() {
        competicion = Competicion.builder().id(1L).nombre("Grupos Test").build();
    }

    // =========================================================
    // soporta()
    // =========================================================

    @Test
    void soporta_gruposPlayoff_devuelveTrue() {
        assertThat(generador.soporta(ConfiguracionCompeticion.FormatoCompeticion.GRUPOS_PLAYOFF)).isTrue();
    }

    @Test
    void soporta_otrosFormatos_devuelveFalse() {
        assertThat(generador.soporta(ConfiguracionCompeticion.FormatoCompeticion.LIGA)).isFalse();
        assertThat(generador.soporta(ConfiguracionCompeticion.FormatoCompeticion.PLAYOFF)).isFalse();
    }

    // =========================================================
    // generar() — número de eventos
    // =========================================================

    @Test
    void generar_seisEquiposDosGrupos_generaEventosDeGruposSeparados() {
        // 6 equipos, numEquiposPlayoff=4 → numGrupos = max(2, 4/2) = 2
        // Grupo 1: 3 equipos → C(3,2) = 3 partidos
        // Grupo 2: 3 equipos → 3 partidos
        // Total: 6 partidos
        List<Equipo> equipos = crearEquipos(6);
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .numEquiposPlayoff(4).build();

        List<Evento> eventos = generador.generar(competicion, equipos, fechaInicio, 7, config);

        assertThat(eventos).hasSize(6);
    }

    @Test
    void generar_ochoEquiposCuatroGrupos_generaEventosDeGruposSeparados() {
        // 8 equipos, numEquiposPlayoff=8 → numGrupos = max(2, 8/2)=4, maxGrupos=8/3=2 → min(4,2)=2 grupos
        // 2 grupos de 4 → C(4,2) × 2 = 6 × 2 = 12 partidos
        List<Equipo> equipos = crearEquipos(8);
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .numEquiposPlayoff(4).build();

        List<Evento> eventos = generador.generar(competicion, equipos, fechaInicio, 7, config);

        // 2 grupos de 4 equipos → 6 + 6 = 12 partidos
        assertThat(eventos).hasSize(12);
    }

    // =========================================================
    // generar() — etiquetas de grupo
    // =========================================================

    @Test
    void generar_todosLosEventosTienenEtiquetaDeGrupo() {
        List<Equipo> equipos = crearEquipos(6);
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .numEquiposPlayoff(4).build();

        List<Evento> eventos = generador.generar(competicion, equipos, fechaInicio, 7, config);

        eventos.forEach(e ->
            assertThat(e.getObservaciones())
                    .as("Todos los eventos deben tener etiqueta de grupo")
                    .startsWith("Grupo ")
        );
    }

    @Test
    void generar_seisEquiposDosGrupos_etiquetasGrupo1Y2() {
        List<Equipo> equipos = crearEquipos(6);
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .numEquiposPlayoff(4).build();

        List<Evento> eventos = generador.generar(competicion, equipos, fechaInicio, 7, config);

        long grupo1Count = eventos.stream().filter(e -> "Grupo 1".equals(e.getObservaciones())).count();
        long grupo2Count = eventos.stream().filter(e -> "Grupo 2".equals(e.getObservaciones())).count();

        assertThat(grupo1Count).isGreaterThan(0);
        assertThat(grupo2Count).isGreaterThan(0);
        assertThat(grupo1Count + grupo2Count).isEqualTo(eventos.size());
    }

    // =========================================================
    // generar() — ningún equipo null en los eventos
    // =========================================================

    @Test
    void generar_ningUnEventoTieneEquipoNull() {
        List<Equipo> equipos = crearEquipos(6);
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .numEquiposPlayoff(4).build();

        List<Evento> eventos = generador.generar(competicion, equipos, fechaInicio, 7, config);

        eventos.forEach(e ->
            e.getEquipos().forEach(ee ->
                assertThat(ee.getEquipo()).isNotNull()
            )
        );
    }

    // =========================================================
    // generar() — validación: menos de 6 equipos
    // =========================================================

    @Test
    void generar_menosDe6Equipos_lanzaIllegalState() {
        List<Equipo> equipos = crearEquipos(5);
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .numEquiposPlayoff(4).build();

        assertThatThrownBy(() -> generador.generar(competicion, equipos, fechaInicio, 7, config))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 6 equipos");
    }

    // =========================================================
    // Helpers
    // =========================================================

    private List<Equipo> crearEquipos(int n) {
        return IntStream.rangeClosed(1, n)
                .mapToObj(i -> Equipo.builder().id((long) i).nombre("Equipo " + i).build())
                .toList();
    }
}
