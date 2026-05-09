package com.compapptition.api.mapper;

import com.compapptition.api.dto.ticket.TicketDetalleDTO;
import com.compapptition.api.dto.ticket.TicketSimpleDTO;
import com.compapptition.api.entity.EstadoTicket;
import com.compapptition.api.entity.Ticket;
import com.compapptition.api.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del mapper MapStruct de Ticket: que serialice el enum como String,
 * extraiga los datos del autor anidado correctamente y maneje nulos sin
 * romper.
 */
@ExtendWith(SpringExtension.class)
@Import(TicketMapperImpl.class)
class TicketMapperTest {

    @Autowired
    private TicketMapper mapper;

    private Usuario autorBase() {
        return Usuario.builder()
                .id(7L)
                .username("alberto.m")
                .email("alberto@test.com")
                .build();
    }

    private Ticket ticketBase() {
        LocalDateTime ts = LocalDateTime.of(2026, 5, 2, 18, 30);
        return Ticket.builder()
                .id(42L)
                .usuario(autorBase())
                .asunto("No me llega el email de invitación")
                .descripcion("He probado a registrar y reenviar pero el correo no aparece.")
                .estado(EstadoTicket.ABIERTO)
                .fechaCreacion(ts)
                .fechaActualizacion(ts)
                .build();
    }

    // =========================================================
    // toSimpleDTO()
    // =========================================================

    @Test
    void toSimpleDTO_mapeaCamposBasicos() {
        TicketSimpleDTO dto = mapper.toSimpleDTO(ticketBase());

        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getUsuarioId()).isEqualTo(7L);
        assertThat(dto.getUsuarioUsername()).isEqualTo("alberto.m");
        assertThat(dto.getAsunto()).isEqualTo("No me llega el email de invitación");
        assertThat(dto.getEstado()).isEqualTo("ABIERTO");
        assertThat(dto.getFechaCreacion()).isNotNull();
        assertThat(dto.getFechaActualizacion()).isNotNull();
    }

    @Test
    void toSimpleDTO_serializaEstadoComoString_paraTodosLosEstados() {
        for (EstadoTicket e : EstadoTicket.values()) {
            Ticket t = ticketBase();
            t.setEstado(e);

            TicketSimpleDTO dto = mapper.toSimpleDTO(t);

            assertThat(dto.getEstado())
                    .as("estado serializado para %s", e)
                    .isEqualTo(e.name());
        }
    }

    @Test
    void toSimpleDTO_estadoNull_devuelveNullSinPetar() {
        Ticket t = ticketBase();
        t.setEstado(null);

        TicketSimpleDTO dto = mapper.toSimpleDTO(t);

        assertThat(dto.getEstado()).isNull();
    }

    // =========================================================
    // toDetalleDTO()
    // =========================================================

    @Test
    void toDetalleDTO_incluyeDescripcionYEmail() {
        TicketDetalleDTO dto = mapper.toDetalleDTO(ticketBase());

        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getDescripcion())
                .isEqualTo("He probado a registrar y reenviar pero el correo no aparece.");
        assertThat(dto.getUsuarioEmail()).isEqualTo("alberto@test.com");
        assertThat(dto.getUsuarioUsername()).isEqualTo("alberto.m");
        assertThat(dto.getEstado()).isEqualTo("ABIERTO");
    }

    // =========================================================
    // toSimpleDTOList()
    // =========================================================

    @Test
    void toSimpleDTOList_mapeaCadaElemento() {
        Ticket a = ticketBase();
        Ticket b = ticketBase();
        b.setId(43L);
        b.setEstado(EstadoTicket.RESUELTO);

        var list = mapper.toSimpleDTOList(java.util.List.of(a, b));

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(42L);
        assertThat(list.get(0).getEstado()).isEqualTo("ABIERTO");
        assertThat(list.get(1).getId()).isEqualTo(43L);
        assertThat(list.get(1).getEstado()).isEqualTo("RESUELTO");
    }
}
