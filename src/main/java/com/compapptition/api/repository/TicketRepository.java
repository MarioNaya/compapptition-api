package com.compapptition.api.repository;

import com.compapptition.api.entity.EstadoTicket;
import com.compapptition.api.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para tickets de soporte. Ofrece consultas paginadas
 * por autor (vista "mis tickets") y vista global filtrable por estado
 * para el admin de sistema.
 *
 * @author Mario
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Tickets de un usuario, ordenados de más reciente a más antiguo.
     * Vista "mis tickets" del autor.
     *
     * @param usuarioId identificador del autor
     * @param pageable  configuración de paginación
     * @return página de tickets del usuario
     */
    Page<Ticket> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    /**
     * Tickets globales ordenados por fecha descendente. Reservado al admin
     * de sistema.
     *
     * @param pageable configuración de paginación
     * @return página de tickets ordenados por fecha
     */
    Page<Ticket> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    /**
     * Tickets globales filtrados por estado, ordenados por fecha descendente.
     * Reservado al admin de sistema (filtros del panel admin).
     *
     * @param estado   estado por el que filtrar
     * @param pageable configuración de paginación
     * @return página de tickets en el estado indicado
     */
    Page<Ticket> findByEstadoOrderByFechaCreacionDesc(EstadoTicket estado, Pageable pageable);

    /**
     * Cuenta tickets en estado ABIERTO o EN_PROCESO. Útil para mostrar
     * un badge en el panel admin con el volumen de tickets pendientes
     * de atención.
     *
     * @return número de tickets activos
     */
    long countByEstadoIn(Iterable<EstadoTicket> estados);
}
