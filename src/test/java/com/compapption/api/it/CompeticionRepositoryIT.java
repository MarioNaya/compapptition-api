package com.compapption.api.it;

import com.compapption.api.entity.*;
import com.compapption.api.repository.*;
import com.compapption.api.util.BaseRepositoryIT;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CompeticionRepositoryIT extends BaseRepositoryIT {

    @PersistenceContext private EntityManager em;
    @Autowired private CompeticionRepository competicionRepository;
    @Autowired private DeporteRepository deporteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EquipoRepository equipoRepository;

    private Deporte deporte;
    private Usuario creador;

    @BeforeEach
    void setUp() {
        deporte = deporteRepository.save(Deporte.builder().nombre("Fútbol").build());
        creador = usuarioRepository.save(
                Usuario.builder().username("admin").email("admin@test.com").password("hash").build());
    }

    // =========================================================
    // findByIdWithDetails — carga eager de deporte y creador
    // =========================================================

    @Test
    void findByIdWithDetails_devuelveDeporteYCreadorFetched() {
        Competicion c = competicionRepository.save(
                Competicion.builder().nombre("Liga 2025").deporte(deporte).creador(creador).build());
        em.flush();
        em.clear(); // vaciar caché L1 para forzar ejecución de SQL

        Optional<Competicion> result = competicionRepository.findByIdWithDetails(c.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDeporte().getNombre()).isEqualTo("Fútbol");
        assertThat(result.get().getCreador().getUsername()).isEqualTo("admin");
    }

    @Test
    void findByIdWithDetails_idInexistente_devuelveEmpty() {
        Optional<Competicion> result = competicionRepository.findByIdWithDetails(999L);
        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByCreadorId — filtra por usuario creador
    // =========================================================

    @Test
    void findByCreadorId_devuelveSoloCompeticionesDelCreador() {
        Usuario otroCreador = usuarioRepository.save(
                Usuario.builder().username("otro").email("otro@test.com").password("hash").build());

        competicionRepository.save(
                Competicion.builder().nombre("C1").deporte(deporte).creador(creador).build());
        competicionRepository.save(
                Competicion.builder().nombre("C2").deporte(deporte).creador(otroCreador).build());
        competicionRepository.save(
                Competicion.builder().nombre("C3").deporte(deporte).creador(creador).build());

        List<Competicion> resultado = competicionRepository.findByCreadorId(creador.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Competicion::getNombre)
                .containsExactlyInAnyOrder("C1", "C3");
    }

    // =========================================================
    // findByPublicasActivas — solo ACTIVA + publica=true
    // =========================================================

    @Test
    void findByPublicasActivas_devuelveSoloPublicasYActivas() {
        // Esta debería aparecer
        competicionRepository.save(Competicion.builder()
                .nombre("Publica-Activa").deporte(deporte).creador(creador)
                .publica(true).estado(Competicion.EstadoCompeticion.ACTIVA).build());
        // Privada + activa → no
        competicionRepository.save(Competicion.builder()
                .nombre("Privada-Activa").deporte(deporte).creador(creador)
                .publica(false).estado(Competicion.EstadoCompeticion.ACTIVA).build());
        // Pública + borrador → no
        competicionRepository.save(Competicion.builder()
                .nombre("Publica-Borrador").deporte(deporte).creador(creador)
                .publica(true).estado(Competicion.EstadoCompeticion.BORRADOR).build());

        Page<Competicion> result = competicionRepository.findByPublicasActivas(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNombre()).isEqualTo("Publica-Activa");
    }

    // =========================================================
    // findByIdWithEquipos — JOIN FETCH de equipos inscritos
    // =========================================================

    @Test
    void findByIdWithEquipos_cargaEquiposInscritos() {
        Equipo equipo = equipoRepository.save(Equipo.builder().nombre("Test FC").build());
        Competicion c = competicionRepository.save(
                Competicion.builder().nombre("Copa").deporte(deporte).creador(creador).build());
        em.persist(CompeticionEquipo.builder().competicion(c).equipo(equipo).build());
        em.flush();
        em.clear();

        Optional<Competicion> result = competicionRepository.findByIdWithEquipos(c.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getEquipos()).hasSize(1);
    }
}
