package com.compapption.api.mapper;

import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.entity.Equipo;
import com.compapption.api.entity.EquipoJugador;
import com.compapption.api.entity.Jugador;
import com.compapption.api.mapper.EquipoJugadorMapperImpl;
import com.compapption.api.mapper.EquipoMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({EquipoMapperImpl.class, EquipoJugadorMapperImpl.class})
class EquipoMapperTest {

    @Autowired
    private EquipoMapper mapper;

    // =========================================================
    // toDetalleDTO() — numJugadores cuenta solo jugadores activos
    // =========================================================

    @Test
    void toDetalleDTO_contaSoloJugadoresActivos() {
        Jugador j1 = Jugador.builder().id(1L).nombre("Juan").build();
        Jugador j2 = Jugador.builder().id(2L).nombre("Pedro").build();
        Jugador j3 = Jugador.builder().id(3L).nombre("Luis").build();

        Set<EquipoJugador> jugadores = new HashSet<>();
        jugadores.add(EquipoJugador.builder().jugador(j1).activo(true).build());
        jugadores.add(EquipoJugador.builder().jugador(j2).activo(true).build());
        jugadores.add(EquipoJugador.builder().jugador(j3).activo(false).build()); // dado de baja

        Equipo equipo = Equipo.builder().id(1L).nombre("Test FC").jugadores(jugadores).build();

        EquipoDetalleDTO dto = mapper.toDetalleDTO(equipo);

        assertThat(dto.getNumJugadores()).isEqualTo(2);
    }

    @Test
    void toDetalleDTO_sinJugadores_numJugadoresCero() {
        Equipo equipo = Equipo.builder().id(1L).nombre("Test FC").build();

        EquipoDetalleDTO dto = mapper.toDetalleDTO(equipo);

        assertThat(dto.getNumJugadores()).isEqualTo(0);
    }

    @Test
    void toDetalleDTO_listaJugadoresSoloContieneLosActivos() {
        Jugador activo = Jugador.builder().id(1L).nombre("Activo").build();
        Jugador inactivo = Jugador.builder().id(2L).nombre("Inactivo").build();

        Set<EquipoJugador> jugadores = new HashSet<>();
        jugadores.add(EquipoJugador.builder().jugador(activo).activo(true).build());
        jugadores.add(EquipoJugador.builder().jugador(inactivo).activo(false).build());

        Equipo equipo = Equipo.builder().id(1L).nombre("Test FC").jugadores(jugadores).build();

        EquipoDetalleDTO dto = mapper.toDetalleDTO(equipo);

        assertThat(dto.getJugadores()).hasSize(1);
        assertThat(dto.getJugadores().get(0).getNombre()).isEqualTo("Activo");
    }

    // =========================================================
    // toSimpleDTO() — campos básicos
    // =========================================================

    @Test
    void toSimpleDTO_mapeaCamposBasicos() {
        Equipo equipo = Equipo.builder()
                .id(10L)
                .nombre("Equipo Simple")
                .build();

        EquipoSimpleDTO dto = mapper.toSimpleDTO(equipo);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getNombre()).isEqualTo("Equipo Simple");
    }
}
