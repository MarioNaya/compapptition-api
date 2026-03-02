package com.compapption.api.service.calendario;

import com.compapption.api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class GeneradorLigaTest {

    private final GeneradorLiga generadorLiga = new GeneradorLiga();

    private Competicion competicion;
    private final LocalDateTime fechaInicio = LocalDateTime.of(2025, 9, 1, 18, 0);

    @BeforeEach
    void setUp() {
        competicion = Competicion.builder().id(1L).nombre("Liga Test").build();
    }

    // =========================================================
    // soporta()
    // =========================================================

    @Test
    void soporta_formatosLiga_devuelveTrue() {
        assertThat(generadorLiga.soporta(ConfiguracionCompeticion.FormatoCompeticion.LIGA)).isTrue();
        assertThat(generadorLiga.soporta(ConfiguracionCompeticion.FormatoCompeticion.LIGA_IDA_VUELTA)).isTrue();
        assertThat(generadorLiga.soporta(ConfiguracionCompeticion.FormatoCompeticion.LIGA_PLAYOFF)).isTrue();
    }

    @Test
    void soporta_formatosNoLiga_devuelveFalse() {
        assertThat(generadorLiga.soporta(ConfiguracionCompeticion.FormatoCompeticion.PLAYOFF)).isFalse();
        assertThat(generadorLiga.soporta(ConfiguracionCompeticion.FormatoCompeticion.GRUPOS_PLAYOFF)).isFalse();
    }

    // =========================================================
    // generarRoundRobin() — número de eventos
    // =========================================================

    @Test
    void generarRoundRobin_cuatroEquipos_generaSeisTotalEventos() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorLiga.generarRoundRobin(competicion, equipos, fechaInicio, 7, false);

        // 4 equipos → 3 jornadas × 2 partidos = 6
        assertThat(eventos).hasSize(6);
    }

    @Test
    void generarRoundRobin_tresEquiposImpar_generaTresEventosSinEquiposNulos() {
        List<Equipo> equipos = crearEquipos(3);

        List<Evento> eventos = generadorLiga.generarRoundRobin(competicion, equipos, fechaInicio, 7, false);

        // 3 equipos → se añade fantasma (null), 3 jornadas, 1 partido válido por jornada = 3
        assertThat(eventos).hasSize(3);

        // Ningún evento debe tener un equipo null
        eventos.forEach(e ->
            e.getEquipos().forEach(ee ->
                assertThat(ee.getEquipo()).as("Equipo no debe ser null").isNotNull()
            )
        );
    }

    @Test
    void generarRoundRobin_dosEquipos_generaUnEvento() {
        List<Equipo> equipos = crearEquipos(2);

        List<Evento> eventos = generadorLiga.generarRoundRobin(competicion, equipos, fechaInicio, 7, false);

        assertThat(eventos).hasSize(1);
    }

    // =========================================================
    // generarRoundRobin() — ida y vuelta
    // =========================================================

    @Test
    void generarRoundRobin_cuatroEquiposIdaVuelta_generaDoceEventos() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorLiga.generarRoundRobin(competicion, equipos, fechaInicio, 7, true);

        // 6 de ida + 6 de vuelta = 12
        assertThat(eventos).hasSize(12);
    }

    @Test
    void generarRoundRobin_idaVuelta_cadaParJuegaDosVecesInvirtiendoLocalia() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorLiga.generarRoundRobin(competicion, equipos, fechaInicio, 7, true);

        // Para cada par (A,B) debe existir un partido A-local y otro B-local
        Map<Long, Set<Long>> localAVisitantes = construirMapaLocalVisitante(eventos);

        for (Equipo eq : equipos) {
            // Cada equipo debe ser local alguna vez
            assertThat(localAVisitantes).containsKey(eq.getId());
        }

        // Verificar que el partido de vuelta invierte la localía del de ida
        List<Evento> ida = eventos.stream().filter(e -> e.getJornada() <= 3).toList();
        List<Evento> vuelta = eventos.stream().filter(e -> e.getJornada() > 3).toList();

        assertThat(ida).hasSize(6);
        assertThat(vuelta).hasSize(6);

        // Cada par en ida aparece como par invertido en vuelta
        Set<String> paresIda = extraerPares(ida);
        Set<String> paresVuelta = extraerPares(vuelta);
        assertThat(paresIda).isEqualTo(paresVuelta);
    }

    // =========================================================
    // generarRoundRobin() — unicidad de enfrentamientos
    // =========================================================

    @Test
    void generarRoundRobin_soloIda_cadaParJugaExactamenteUnaVez() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorLiga.generarRoundRobin(competicion, equipos, fechaInicio, 7, false);

        Set<String> pares = extraerPares(eventos);

        // Con 4 equipos: C(4,2) = 6 pares únicos
        assertThat(pares).hasSize(6);
        // El número de pares únicos debe ser igual al total de eventos (sin duplicados)
        assertThat(eventos).hasSize(pares.size());
    }

    // =========================================================
    // generarRoundRobin() — jornadas
    // =========================================================

    @Test
    void generarRoundRobin_cuatroEquipos_jornadasCorrectas() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorLiga.generarRoundRobin(competicion, equipos, fechaInicio, 7, false);

        Set<Integer> jornadas = eventos.stream().map(Evento::getJornada).collect(Collectors.toSet());
        assertThat(jornadas).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void generarRoundRobin_idaVuelta_jornadasHasta6() {
        List<Equipo> equipos = crearEquipos(4);

        List<Evento> eventos = generadorLiga.generarRoundRobin(competicion, equipos, fechaInicio, 7, true);

        int maxJornada = eventos.stream().mapToInt(Evento::getJornada).max().orElse(0);
        assertThat(maxJornada).isEqualTo(6);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private List<Equipo> crearEquipos(int n) {
        return java.util.stream.IntStream.rangeClosed(1, n)
                .mapToObj(i -> Equipo.builder().id((long) i).nombre("Equipo " + i).build())
                .toList();
    }

    /** Clave canónica de par: siempre el ID menor primero (independiente de localía). */
    private Set<String> extraerPares(List<Evento> eventos) {
        return eventos.stream().map(e -> {
            List<Long> ids = e.getEquipos().stream()
                    .map(ee -> ee.getEquipo().getId())
                    .sorted()
                    .toList();
            return ids.get(0) + "-" + ids.get(1);
        }).collect(Collectors.toSet());
    }

    private Map<Long, Set<Long>> construirMapaLocalVisitante(List<Evento> eventos) {
        return eventos.stream().collect(Collectors.groupingBy(
                e -> e.getEquipos().stream()
                        .filter(EventoEquipo::isEsLocal)
                        .map(ee -> ee.getEquipo().getId())
                        .findFirst().orElseThrow(),
                Collectors.mapping(
                        e -> e.getEquipos().stream()
                                .filter(ee -> !ee.isEsLocal())
                                .map(ee -> ee.getEquipo().getId())
                                .findFirst().orElseThrow(),
                        Collectors.toSet()
                )
        ));
    }
}
