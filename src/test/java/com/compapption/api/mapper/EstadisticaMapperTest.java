package com.compapption.api.mapper;

import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.entity.*;
import com.compapption.api.mapper.EstadisticaMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(EstadisticaMapperImpl.class)
class EstadisticaMapperTest {

    @Autowired
    private EstadisticaMapper mapper;

    // =========================================================
    // jugadorNombre — expresión custom con concatenación
    // =========================================================

    @Test
    void toDTO_conApellidos_combinaNombreYApellidos() {
        EstadisticaJugadorEvento estadistica = estadisticaCon("Juan", "García");

        EstadisticaJugadorDTO dto = mapper.toDTO(estadistica);

        assertThat(dto.getJugadorNombre()).isEqualTo("Juan García");
    }

    @Test
    void toDTO_sinApellidos_nombreConEspacioFinal() {
        EstadisticaJugadorEvento estadistica = estadisticaCon("Solo", null);

        EstadisticaJugadorDTO dto = mapper.toDTO(estadistica);

        // La expression añade "" cuando apellidos es null → "Solo "
        assertThat(dto.getJugadorNombre()).isEqualTo("Solo ");
    }

    // =========================================================
    // Mapeo de IDs anidados
    // =========================================================

    @Test
    void toDTO_mapeaIdsAnidadosCorrectamente() {
        Evento evento = Evento.builder().id(5L).build();
        Jugador jugador = Jugador.builder().id(10L).nombre("Ana").apellidos("López").build();
        TipoEstadistica tipo = TipoEstadistica.builder().id(20L).nombre("Goles").build();

        EstadisticaJugadorEvento estadistica = EstadisticaJugadorEvento.builder()
                .evento(evento)
                .jugador(jugador)
                .tipoEstadistica(tipo)
                .valor(new BigDecimal("3"))
                .build();

        EstadisticaJugadorDTO dto = mapper.toDTO(estadistica);

        assertThat(dto.getEventoId()).isEqualTo(5L);
        assertThat(dto.getJugadorId()).isEqualTo(10L);
        assertThat(dto.getTipoEstadisticaId()).isEqualTo(20L);
        assertThat(dto.getTipoEstadisticaNombre()).isEqualTo("Goles");
    }

    // =========================================================
    // Helpers
    // =========================================================

    private EstadisticaJugadorEvento estadisticaCon(String nombre, String apellidos) {
        Jugador jugador = Jugador.builder().id(1L).nombre(nombre).apellidos(apellidos).build();
        TipoEstadistica tipo = TipoEstadistica.builder().id(10L).nombre("Tipo").build();
        return EstadisticaJugadorEvento.builder()
                .jugador(jugador)
                .tipoEstadistica(tipo)
                .valor(BigDecimal.ONE)
                .build();
    }
}
