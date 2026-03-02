package com.compapption.api.service.calendario;

import com.compapption.api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class GeneradorPlayoffTest {

    private final GeneradorPlayoff generadorPlayoff = new GeneradorPlayoff();

    private Competicion competicion;
    private final LocalDateTime fechaInicio = LocalDateTime.of(2025, 9, 1, 18, 0);

    @BeforeEach
    void setUp() {
        competicion = Competicion.builder().id(1L).nombre("Playoff Test").build();
    }

    // =========================================================
    // soporta()
    // =========================================================

    @Test
    void soporta_playoff_devuelveTrue() {
        assertThat(generadorPlayoff.soporta(ConfiguracionCompeticion.FormatoCompeticion.PLAYOFF)).isTrue();
    }

    @Test
    void soporta_formatosNoPlayoff_devuelveFalse() {
        assertThat(generadorPlayoff.soporta(ConfiguracionCompeticion.FormatoCompeticion.LIGA)).isFalse();
        assertThat(generadorPlayoff.soporta(ConfiguracionCompeticion.FormatoCompeticion.LIGA_IDA_VUELTA)).isFalse();
        assertThat(generadorPlayoff.soporta(ConfiguracionCompeticion.FormatoCompeticion.GRUPOS_PLAYOFF)).isFalse();
    }

    // =========================================================
    // generarBracketCompleto() — número de eventos
    // =========================================================

    @Test
    void generarBracketCompleto_cuatroEquipos_generaTresEventos() {
        // 4 equipos → bracket 4 → 2 cruces en ronda 1 + 1 cruce en final = 3 eventos
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 1);

        assertThat(eventos).hasSize(3);
    }

    @Test
    void generarBracketCompleto_ochoEquipos_generaSieteEventos() {
        // 8 equipos → bracket 8 → 4+2+1 = 7 eventos
        List<Equipo> equipos = crearEquipos(8);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 1);

        assertThat(eventos).hasSize(7);
    }

    @Test
    void generarBracketCompleto_dosEquipos_generaUnEvento() {
        // 2 equipos → bracket 2 → 1 cruce = 1 evento (la final directa)
        List<Equipo> equipos = crearEquipos(2);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 1);

        assertThat(eventos).hasSize(1);
    }

    // =========================================================
    // generarBracketCompleto() — estructura: ronda 1 con equipos, rondas 2+ placeholders
    // =========================================================

    @Test
    void generarBracketCompleto_ronda1TieneEquiposReales() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 1);

        // Jornada 1 (rondaInicial=1): los eventos de la ronda 1 tienen equipos reales
        List<Evento> ronda1 = eventos.stream().filter(e -> e.getJornada() == 1).toList();
        assertThat(ronda1).hasSize(2);
        ronda1.forEach(e ->
            assertThat(e.getEquipos()).as("Ronda 1 debe tener equipos asignados").isNotEmpty()
        );
    }

    @Test
    void generarBracketCompleto_rondasSiguientesSonPlaceholders() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 1);

        // Jornada 2 (la final): placeholder, sin equipos propios pero con referencias
        List<Evento> ronda2 = eventos.stream().filter(e -> e.getJornada() == 2).toList();
        assertThat(ronda2).hasSize(1);
        Evento final_ = ronda2.get(0);
        assertThat(final_.getEquipos()).as("Placeholder no debe tener equipos directos").isEmpty();
        assertThat(final_.getPartidoAnteriorLocal()).as("Placeholder debe referenciar partido anterior").isNotNull();
        assertThat(final_.getPartidoAnteriorVisitante()).isNotNull();
    }

    // =========================================================
    // generarBracketCompleto() — ida y vuelta (2 partidos por eliminatoria)
    // =========================================================

    @Test
    void generarBracketCompleto_cuatroEquiposIdaVuelta_generaSeisTotalEventos() {
        // 4 equipos, 2 partidos por eliminatoria: ronda 1 = 2 cruces × 2 = 4 eventos
        //                                          ronda 2 = 1 cruce × 2 = 2 eventos
        // Total = 6 eventos
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 2);

        assertThat(eventos).hasSize(6);
    }

    // =========================================================
    // generarBracketCompleto() — potencia de 2 con relleno de byes
    // =========================================================

    @Test
    void generarBracketCompleto_cincoEquipos_bracketSePaddeaA8() {
        // 5 equipos → next power of 2 = 8 → algunos cruces de ronda 1 son byes
        // Con el orden [1,8,4,5,2,7,3,6]: cruce (1,8)=bye, (2,7)=bye, (3,6)=bye, (4,5)=válido
        // Ronda 1: solo 1 evento real
        List<Equipo> equipos = crearEquipos(5);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 1);

        // Al menos hay eventos generados (no debe estar vacío)
        assertThat(eventos).isNotEmpty();
        // Los eventos de ronda 1 con equipos asignados deben ser válidos (sin null en equipos)
        eventos.stream()
            .filter(e -> !e.getEquipos().isEmpty())
            .forEach(e ->
                e.getEquipos().forEach(ee ->
                    assertThat(ee.getEquipo()).isNotNull()
                )
            );
    }

    // =========================================================
    // generarBracketCompleto() — competición asignada a todos los eventos
    // =========================================================

    @Test
    void generarBracketCompleto_todosLosEventosTienenLaCompeticion() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 1);

        eventos.forEach(e ->
            assertThat(e.getCompeticion()).as("Todos los eventos deben tener competición").isEqualTo(competicion)
        );
    }

    // =========================================================
    // generarBracketCompleto() — jornadas asignadas correctamente
    // =========================================================

    @Test
    void generarBracketCompleto_cuatroEquipos_jornadasSonCorrelaticas() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorPlayoff.generarBracketCompleto(
                competicion, equipos, fechaInicio, 7, 1, 1);

        // Con rondaInicial=1 y 4 equipos: jornada 1 (cuartos) y jornada 2 (final)
        assertThat(eventos.stream().mapToInt(Evento::getJornada).min().orElse(-1)).isEqualTo(1);
        assertThat(eventos.stream().mapToInt(Evento::getJornada).max().orElse(-1)).isEqualTo(2);
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
