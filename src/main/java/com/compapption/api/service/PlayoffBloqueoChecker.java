package com.compapption.api.service;

import com.compapption.api.entity.Evento;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Componente puro que centraliza la regla de "evento de playoff bloqueado".
 * Sustituye a la duplicación previa entre {@code EventoService.estaBloqueado}
 * y {@code EstadisticaService.eventoBloqueado} (cierra A-5).
 *
 * <p>Reglas:
 * <ul>
 *   <li>Eventos de fase regular ({@code numeroPartido == null}) nunca se
 *       consideran bloqueados.</li>
 *   <li>Si el evento depende de partidos anteriores del bracket, esos deben
 *       estar {@code FINALIZADO} para permitir edición del actual.</li>
 *   <li>Si es primera ronda de playoff (sin partidos previos del bracket),
 *       se bloquea hasta que se cierre la fase regular (liga o grupos).</li>
 * </ul>
 *
 * <p>Vive en su propia clase para que {@link EstadisticaService} y
 * {@link EventoService} puedan inyectarlo sin reintroducir la dependencia
 * circular que motivó la duplicación original.</p>
 */
@Component
@RequiredArgsConstructor
public class PlayoffBloqueoChecker {

    private final EventoRepository eventoRepository;

    public boolean estaBloqueado(Evento e) {
        if (e.getNumeroPartido() == null) return false;
        Evento ant1 = e.getPartidoAnteriorLocal();
        Evento ant2 = e.getPartidoAnteriorVisitante();
        if (ant1 != null && ant1.getEstado() != Evento.EstadoEvento.FINALIZADO) return true;
        if (ant2 != null && ant2.getEstado() != Evento.EstadoEvento.FINALIZADO) return true;
        if (ant1 == null && ant2 == null) {
            return eventoRepository.existsFaseRegularNoFinalizada(e.getCompeticion().getId());
        }
        return false;
    }

    /**
     * Lanza {@link BadRequestException} con mensaje uniforme si el evento está
     * bloqueado. Permite que servicios que necesitan abortar (registrar
     * resultado, registrar estadística) compartan el mismo contrato de error.
     */
    public void asegurarNoBloqueado(Evento e) {
        if (estaBloqueado(e)) {
            throw new BadRequestException(
                    "El partido de playoff aún no se puede editar: hay rondas anteriores o la fase regular sin finalizar");
        }
    }
}
