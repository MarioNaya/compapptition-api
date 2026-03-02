package com.compapption.api.it;

import com.compapption.api.entity.*;
import com.compapption.api.repository.*;
import com.compapption.api.util.BaseRepositoryIT;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClasificacionRepositoryIT extends BaseRepositoryIT {

    @PersistenceContext private EntityManager em;
    @Autowired private ClasificacionRepository clasificacionRepository;
    @Autowired private CompeticionRepository competicionRepository;
    @Autowired private DeporteRepository deporteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EquipoRepository equipoRepository;

    private Competicion competicion;
    private Equipo equipo1, equipo2, equipo3;

    @BeforeEach
    void setUp() {
        Deporte deporte = deporteRepository.save(Deporte.builder().nombre("Fútbol").build());
        Usuario creador = usuarioRepository.save(
                Usuario.builder().username("admin").email("admin@test.com").password("hash").build());
        competicion = competicionRepository.save(
                Competicion.builder().nombre("Liga").deporte(deporte).creador(creador).build());
        equipo1 = equipoRepository.save(Equipo.builder().nombre("Equipo A").build());
        equipo2 = equipoRepository.save(Equipo.builder().nombre("Equipo B").build());
        equipo3 = equipoRepository.save(Equipo.builder().nombre("Equipo C").build());
    }

    private Clasificacion saveClasificacion(Equipo equipo, Integer temporada, Integer posicion, Integer puntos) {
        return clasificacionRepository.save(Clasificacion.builder()
                .competicion(competicion)
                .equipo(equipo)
                .temporada(temporada)
                .posicion(posicion)
                .puntos(puntos)
                .build());
    }

    // =========================================================
    // findByCompeticionIdAndTemporada
    // =========================================================

    @Test
    void findByCompeticionIdAndTemporada_filtraPorTemporada() {
        saveClasificacion(equipo1, 1, 1, 9);
        saveClasificacion(equipo2, 1, 2, 6);
        saveClasificacion(equipo3, 2, 1, 3); // temporada diferente

        List<Clasificacion> result = clasificacionRepository.findByCompeticionIdAndTemporada(
                competicion.getId(), 1);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getTemporada() == 1);
    }

    // =========================================================
    // findByCompeticionIdOrderByPosicion
    // =========================================================

    @Test
    void findByCompeticionIdOrderByPosicion_devuelveOrdenAscendente() {
        saveClasificacion(equipo1, 1, 3, 3);
        saveClasificacion(equipo2, 1, 1, 9);
        saveClasificacion(equipo3, 1, 2, 6);

        List<Clasificacion> result = clasificacionRepository
                .findByCompeticionIdOrderByPosicion(competicion.getId());

        assertThat(result).extracting(Clasificacion::getPosicion)
                .containsExactly(1, 2, 3);
    }

    // =========================================================
    // findByCompeticionIdAndEquipoId
    // =========================================================

    @Test
    void findByCompeticionIdAndEquipoId_encuentraCorrectamente() {
        saveClasificacion(equipo1, 1, 1, 9);
        saveClasificacion(equipo2, 1, 2, 6);

        Optional<Clasificacion> result = clasificacionRepository
                .findByCompeticionIdAndEquipoId(competicion.getId(), equipo1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getEquipo().getId()).isEqualTo(equipo1.getId());
        assertThat(result.get().getPuntos()).isEqualTo(9);
    }

    // =========================================================
    // findByCompeticionIdAndEquipoIdAndTemporada — filtro por temporada
    // =========================================================

    @Test
    void findByCompeticionIdAndEquipoIdAndTemporada_filtraTresColumnas() {
        saveClasificacion(equipo1, 1, 1, 9);
        saveClasificacion(equipo1, 2, 1, 15); // misma competicion+equipo, diferente temporada

        Optional<Clasificacion> result = clasificacionRepository
                .findByCompeticionIdAndEquipoIdAndTemporada(
                        competicion.getId(), equipo1.getId(), 2);

        assertThat(result).isPresent();
        assertThat(result.get().getPuntos()).isEqualTo(15);
    }

    // =========================================================
    // Unique constraint (competicion_id, equipo_id, temporada)
    // =========================================================

    @Test
    void uniqueConstraint_competicionEquipoTemporada_lanzaExcepcion() {
        saveClasificacion(equipo1, 1, 1, 9);
        em.flush(); // asegurar que el INSERT se envía antes del duplicado

        assertThatThrownBy(() -> {
            saveClasificacion(equipo1, 1, 2, 0); // mismo competicion+equipo+temporada
            em.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
