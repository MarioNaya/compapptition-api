package com.compapption.api.repository;

import com.compapption.api.entity.Equipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Equipo.
 * Gestiona la búsqueda de equipos por manager, jugador, competición y nombre,
 * así como la carga eager de la colección de jugadores.
 *
 * @author Mario
 */
@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    /**
     * Busca un equipo por su identificador cargando en la misma consulta
     * la colección de jugadores inscritos (evita N+1).
     *
     * @param id identificador del equipo
     * @return Optional con el equipo y sus jugadores, vacío si no existe
     */
    @Query("SELECT e FROM Equipo e " +
            "LEFT JOIN FETCH e.jugadores ej " +
            "LEFT JOIN FETCH ej.jugador " +
            "WHERE e.id = :id")
    Optional<Equipo> findByIdWithJugadores(
            @Param("id") long id
    );

    /**
     * Obtiene todos los equipos en los que un usuario es manager.
     *
     * @param usuarioId identificador del usuario manager
     * @return lista de equipos gestionados por el usuario
     */
    @Query("SELECT DISTINCT e FROM Equipo e " +
            "JOIN e.managers m " +
            "WHERE m.usuario.id = :usuarioId")
    List<Equipo> findByManagersId(
            @Param("usuarioId") long usuarioId
    );

    /**
     * Obtiene todos los equipos en los que un usuario figura como jugador activo.
     *
     * @param usuarioId identificador del usuario vinculado al jugador
     * @return lista de equipos a los que pertenece el jugador del usuario
     */
    @Query("SELECT DISTINCT e FROM Equipo e " +
            "JOIN e.jugadores ej " +
            "WHERE ej.jugador.usuario.id = :usuarioId")
    List<Equipo> findByJugadoresId(
        @Param("usuarioId") long usuarioId
    );

    /**
     * Obtiene los equipos activos inscritos en una competición concreta.
     *
     * @param competicionId identificador de la competición
     * @return lista de equipos activos en la competición
     */
    @Query("SELECT DISTINCT e FROM Equipo e " +
            "JOIN e.competiciones ce " +
            "WHERE ce.competicion.id = :competicionId " +
            "AND ce.activo = true")
    List<Equipo> findByCompeticionId(
            @Param("competicionId") long competicionId
    );

    /**
     * Busca equipos cuyo nombre contenga el texto indicado (insensible a mayúsculas),
     * de forma paginada.
     *
     * @param nombre   texto a buscar en el nombre del equipo
     * @param pageable parámetros de paginación y ordenación
     * @return página de equipos cuyo nombre coincide con la búsqueda
     */
    @Query("SELECT e FROM Equipo e " +
            "WHERE LOWER(e.nombre)" +
            "LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Equipo> searchByNombre(
            @Param("nombre") String nombre,
            Pageable pageable
    );

    /**
     * Comprueba si ya existe un equipo con el nombre indicado.
     *
     * @param nombre nombre del equipo a verificar
     * @return {@code true} si el nombre ya está en uso
     */
    boolean existsByNombre(String nombre);
}
