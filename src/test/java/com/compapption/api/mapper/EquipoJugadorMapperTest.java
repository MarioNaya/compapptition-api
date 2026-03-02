package com.compapption.api.mapper;

import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.entity.EquipoJugador;
import com.compapption.api.entity.Jugador;
import com.compapption.api.mapper.EquipoJugadorMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(EquipoJugadorMapperImpl.class)
class EquipoJugadorMapperTest {

    @Autowired
    private EquipoJugadorMapper mapper;

    // =========================================================
    // toJugadorSimpleDTO() — prioridad de dorsal
    // =========================================================

    @Test
    void toJugadorSimpleDTO_conDorsalEquipo_usaDorsalEquipo() {
        Jugador jugador = Jugador.builder().id(1L).nombre("Juan").dorsal(10).build();
        EquipoJugador ej = EquipoJugador.builder()
                .jugador(jugador)
                .dorsalEquipo(99) // tiene dorsal de equipo
                .build();

        JugadorSimpleDTO dto = mapper.toJugadorSimpleDTO(ej);

        assertThat(dto.getDorsal()).isEqualTo(99);
    }

    @Test
    void toJugadorSimpleDTO_sinDorsalEquipo_usaDorsalJugador() {
        Jugador jugador = Jugador.builder().id(1L).nombre("Juan").dorsal(7).build();
        EquipoJugador ej = EquipoJugador.builder()
                .jugador(jugador)
                .dorsalEquipo(null) // sin dorsal de equipo → usa el del jugador
                .build();

        JugadorSimpleDTO dto = mapper.toJugadorSimpleDTO(ej);

        assertThat(dto.getDorsal()).isEqualTo(7);
    }

    @Test
    void toJugadorSimpleDTO_mapeaNombreYApellidosDesdeJugador() {
        Jugador jugador = Jugador.builder().id(5L).nombre("Ana").apellidos("López").build();
        EquipoJugador ej = EquipoJugador.builder().jugador(jugador).build();

        JugadorSimpleDTO dto = mapper.toJugadorSimpleDTO(ej);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getNombre()).isEqualTo("Ana");
        assertThat(dto.getApellidos()).isEqualTo("López");
    }

    // =========================================================
    // toJugadorSimpleDTOList() — filtro por activo
    // =========================================================

    @Test
    void toJugadorSimpleDTOList_filtraJugadoresInactivos() {
        Jugador j1 = Jugador.builder().id(1L).nombre("Activo").build();
        Jugador j2 = Jugador.builder().id(2L).nombre("Inactivo").build();

        EquipoJugador activo = EquipoJugador.builder().jugador(j1).activo(true).build();
        EquipoJugador inactivo = EquipoJugador.builder().jugador(j2).activo(false).build();

        Set<EquipoJugador> set = new HashSet<>();
        set.add(activo);
        set.add(inactivo);

        List<JugadorSimpleDTO> lista = mapper.toJugadorSimpleDTOList(set);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getNombre()).isEqualTo("Activo");
    }

    @Test
    void toJugadorSimpleDTOList_setNulo_devuelveNull() {
        assertThat(mapper.toJugadorSimpleDTOList(null)).isNull();
    }

    @Test
    void toJugadorSimpleDTOList_todosInactivos_devuelveListaVacia() {
        Jugador jugador = Jugador.builder().id(1L).nombre("Baja").build();
        EquipoJugador inactivo = EquipoJugador.builder().jugador(jugador).activo(false).build();

        List<JugadorSimpleDTO> lista = mapper.toJugadorSimpleDTOList(Set.of(inactivo));

        assertThat(lista).isEmpty();
    }
}
