package com.compapption.api.mapper;

import com.compapption.api.dto.eventoDTO.EventoDetalleDTO;
import com.compapption.api.dto.eventoDTO.EventoEquipoDTO;
import com.compapption.api.entity.*;
import com.compapption.api.mapper.EventoMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(EventoMapperImpl.class)
class EventoMapperTest {

    @Autowired
    private EventoMapper mapper;

    // =========================================================
    // extractLocal() — @Named helper
    // =========================================================

    @Test
    void extractLocal_conLocalYVisitante_devuelveEquipoLocal() {
        Equipo localEquipo = Equipo.builder().id(1L).nombre("Local FC").build();
        Equipo visitanteEquipo = Equipo.builder().id(2L).nombre("Visitante FC").build();

        Set<EventoEquipo> equipos = new HashSet<>();
        equipos.add(EventoEquipo.builder().equipo(localEquipo).esLocal(true).build());
        equipos.add(EventoEquipo.builder().equipo(visitanteEquipo).esLocal(false).build());

        EventoEquipoDTO dto = mapper.extractLocal(equipos);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("Local FC");
        assertThat(dto.getEsLocal()).isTrue();
    }

    @Test
    void extractVisitante_conLocalYVisitante_devuelveEquipoVisitante() {
        Equipo localEquipo = Equipo.builder().id(1L).nombre("Local FC").build();
        Equipo visitanteEquipo = Equipo.builder().id(2L).nombre("Visitante FC").build();

        Set<EventoEquipo> equipos = new HashSet<>();
        equipos.add(EventoEquipo.builder().equipo(localEquipo).esLocal(true).build());
        equipos.add(EventoEquipo.builder().equipo(visitanteEquipo).esLocal(false).build());

        EventoEquipoDTO dto = mapper.extractVisitante(equipos);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getNombre()).isEqualTo("Visitante FC");
        assertThat(dto.getEsLocal()).isFalse();
    }

    @Test
    void extractLocal_setNulo_devuelveNull() {
        assertThat(mapper.extractLocal(null)).isNull();
    }

    @Test
    void extractLocal_sinEquipoLocal_devuelveNull() {
        Set<EventoEquipo> equipos = new HashSet<>();
        equipos.add(EventoEquipo.builder()
                .equipo(Equipo.builder().id(2L).nombre("Solo Visitante").build())
                .esLocal(false)
                .build());

        assertThat(mapper.extractLocal(equipos)).isNull();
    }

    // =========================================================
    // toDetalleDTO() — mapeo de campos anidados
    // =========================================================

    @Test
    void toDetalleDTO_mapeaCompeticionIdYNombre() {
        Competicion competicion = Competicion.builder().id(10L).nombre("Liga Test").build();
        Evento evento = Evento.builder()
                .id(1L)
                .competicion(competicion)
                .build();

        EventoDetalleDTO dto = mapper.toDetalleDTO(evento);

        assertThat(dto.getCompeticionId()).isEqualTo(10L);
        assertThat(dto.getCompeticionNombre()).isEqualTo("Liga Test");
    }

    @Test
    void toDetalleDTO_sinEquipos_localYVisitanteNulos() {
        Evento evento = Evento.builder()
                .id(1L)
                .competicion(Competicion.builder().id(1L).build())
                .build();

        EventoDetalleDTO dto = mapper.toDetalleDTO(evento);

        assertThat(dto.getEquipoLocal()).isNull();
        assertThat(dto.getEquipoVisitante()).isNull();
    }
}
