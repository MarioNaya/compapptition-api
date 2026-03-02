package com.compapption.api.it;

import com.compapption.api.entity.*;
import com.compapption.api.repository.*;
import com.compapption.api.util.BaseRepositoryIT;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EventoRepositoryIT extends BaseRepositoryIT {

    @PersistenceContext private EntityManager em;
    @Autowired private EventoRepository eventoRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private CompeticionRepository competicionRepository;
    @Autowired private DeporteRepository deporteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Competicion competicion;

    @BeforeEach
    void setUp() {
        Deporte deporte = deporteRepository.save(Deporte.builder().nombre("Fútbol").build());
        Usuario creador = usuarioRepository.save(
                Usuario.builder().username("admin").email("admin@test.com").password("hash").build());
        competicion = competicionRepository.save(
                Competicion.builder().nombre("Liga Test").deporte(deporte).creador(creador).build());
    }

    private Evento crearEvento(Evento.EstadoEvento estado, Integer temporada) {
        return eventoRepository.save(Evento.builder()
                .competicion(competicion)
                .fechaHora(LocalDateTime.now())
                .estado(estado)
                .temporada(temporada)
                .build());
    }

    // =========================================================
    // findFinalizadosByCompeticionId
    // =========================================================

    @Test
    void findFinalizadosByCompeticionId_devuelveSoloFinalizados() {
        crearEvento(Evento.EstadoEvento.FINALIZADO, 1);
        crearEvento(Evento.EstadoEvento.PROGRAMADO, 1);
        crearEvento(Evento.EstadoEvento.EN_CURSO, 1);

        List<Evento> result = eventoRepository.findFinalizadosByCompeticionId(competicion.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstado()).isEqualTo(Evento.EstadoEvento.FINALIZADO);
    }

    // =========================================================
    // findFinalizadosByCompeticionIdAndTemporada
    // =========================================================

    @Test
    void findFinalizadosByCompeticionIdAndTemporada_filtraPorTemporada() {
        crearEvento(Evento.EstadoEvento.FINALIZADO, 1);
        crearEvento(Evento.EstadoEvento.FINALIZADO, 1);
        crearEvento(Evento.EstadoEvento.FINALIZADO, 2); // diferente temporada

        List<Evento> result = eventoRepository.findFinalizadosByCompeticionIdAndTemporada(
                competicion.getId(), 1);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.getTemporada() == 1);
    }

    // =========================================================
    // findByPartidoAnteriorId — JOIN FETCH de siguiente ronda bracket
    // =========================================================

    @Test
    void findByPartidoAnteriorId_encuentraEventoSiguienteRonda() {
        Evento anterior = crearEvento(Evento.EstadoEvento.FINALIZADO, 1);
        Evento siguiente = eventoRepository.save(Evento.builder()
                .competicion(competicion)
                .fechaHora(LocalDateTime.now())
                .estado(Evento.EstadoEvento.PROGRAMADO)
                .temporada(1)
                .partidoAnteriorLocal(anterior)
                .build());
        em.flush();
        em.clear();

        List<Evento> result = eventoRepository.findByPartidoAnteriorId(anterior.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(siguiente.getId());
    }

    @Test
    void findByPartidoAnteriorId_sinReferencias_devuelveListaVacia() {
        Evento evento = crearEvento(Evento.EstadoEvento.FINALIZADO, 1);

        List<Evento> result = eventoRepository.findByPartidoAnteriorId(evento.getId());

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findMaxJornadaByCompeticionId
    // =========================================================

    @Test
    void findMaxJornadaByCompeticionId_devuelveMaxima() {
        eventoRepository.save(Evento.builder().competicion(competicion)
                .fechaHora(LocalDateTime.now()).estado(Evento.EstadoEvento.PROGRAMADO).jornada(3).build());
        eventoRepository.save(Evento.builder().competicion(competicion)
                .fechaHora(LocalDateTime.now()).estado(Evento.EstadoEvento.PROGRAMADO).jornada(1).build());
        eventoRepository.save(Evento.builder().competicion(competicion)
                .fechaHora(LocalDateTime.now()).estado(Evento.EstadoEvento.PROGRAMADO).jornada(7).build());

        Integer max = eventoRepository.findMaxJornadaByCompeticionId(competicion.getId());

        assertThat(max).isEqualTo(7);
    }

    // =========================================================
    // findByIdWithEquipos — LEFT JOIN FETCH con equipos
    // =========================================================

    @Test
    void findByIdWithEquipos_devuelveEventoConEquipos() {
        Equipo equipo = equipoRepository.save(Equipo.builder().nombre("FC Test").build());
        Evento evento = crearEvento(Evento.EstadoEvento.PROGRAMADO, 1);
        em.persist(EventoEquipo.builder().evento(evento).equipo(equipo).esLocal(true).build());
        em.flush();
        em.clear();

        Optional<Evento> result = eventoRepository.findByIdWithEquipos(evento.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getEquipos()).hasSize(1);
    }
}
