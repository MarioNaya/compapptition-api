package com.compapption.api.repository;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Competicion.
 * Gestiona búsquedas paginadas de competiciones públicas, filtrado por creador,
 * participante y deporte, así como la carga de relaciones con equipos y detalles.
 *
 * @author Mario
 */
@Repository
public interface CompeticionRepository extends JpaRepository<Competicion, Long> {

    /**
     * Obtiene todas las competiciones públicas con estado ACTIVA de forma paginada.
     *
     * @param pageable parámetros de paginación y ordenación
     * @return página de competiciones públicas activas
     */
    @Query("SELECT c FROM Competicion c " +
            "WHERE c.publica = true AND c.estado = 'ACTIVA'")
    Page<Competicion> findByPublicasActivas(Pageable pageable);

    /**
     * Obtiene todas las competiciones creadas por un usuario concreto.
     *
     * @param usuarioId identificador del usuario creador
     * @return lista de competiciones creadas por el usuario
     */
    @Query("SELECT c FROM Competicion c " +
            "WHERE c.creador.id = :usuarioId")
    List<Competicion> findByCreadorId(
            @Param("usuarioId") long usuarioId
    );

    /**
     * Obtiene todas las competiciones en las que un usuario tiene algún rol asignado.
     *
     * @param usuarioId identificador del usuario participante
     * @return lista de competiciones en las que el usuario participa
     */
    @Query("SELECT DISTINCT c FROM Competicion c " +
            "JOIN c.usuariosRol ur " +
            "WHERE ur.usuario.id = :usuarioId")
    List<Competicion> findByUsuarioParticipante(
            @Param("usuarioId") long usuarioId
    );

    /**
     * Obtiene las competiciones en las que un usuario tiene un rol concreto
     * (ADMIN_COMPETICION, MANAGER_EQUIPO, ARBITRO).
     *
     * @param usuarioId identificador del usuario
     * @param rolNombre nombre del rol a filtrar
     * @return lista de competiciones donde el usuario tiene ese rol
     */
    @Query("SELECT DISTINCT c FROM Competicion c " +
            "JOIN c.usuariosRol ur " +
            "WHERE ur.usuario.id = :usuarioId " +
            "AND ur.rol.nombre = :rolNombre")
    List<Competicion> findByUsuarioYRol(
            @Param("usuarioId") long usuarioId,
            @Param("rolNombre") Rol.RolNombre rolNombre
    );

    /**
     * Obtiene las competiciones donde el usuario figura como jugador activo en
     * alguno de los equipos inscritos (camino Usuario→Jugador→EquipoJugador→
     * Equipo→CompeticionEquipo→Competicion). Permite que el dashboard liste
     * competiciones de un jugador aunque no exista UsuarioRolCompeticion.
     *
     * @param usuarioId identificador del usuario vinculado al jugador
     * @return lista de competiciones donde el usuario juega
     */
    @Query("SELECT DISTINCT c FROM Competicion c " +
            "JOIN c.equipos ce " +
            "JOIN ce.equipo e " +
            "JOIN e.jugadores ej " +
            "WHERE ce.activo = true " +
            "AND ej.activo = true " +
            "AND ej.jugador.usuario.id = :usuarioId")
    List<Competicion> findByJugadorUsuarioId(
            @Param("usuarioId") long usuarioId
    );

    /**
     * Busca una competición por su identificador cargando en la misma consulta
     * la colección de equipos inscritos (evita N+1).
     *
     * @param id identificador de la competición
     * @return Optional con la competición e equipos, vacío si no existe
     */
    @Query("SELECT c FROM Competicion c " +
            "LEFT JOIN FETCH c.equipos ce " +
            "LEFT JOIN FETCH ce.equipo " +
            "WHERE c.id = :id")
    Optional<Competicion> findByIdWithEquipos(
            @Param("id") long id
    );

    /**
     * Busca una competición por su identificador cargando deporte, configuración
     * y creador en la misma consulta.
     *
     * @param id identificador de la competición
     * @return Optional con la competición y sus detalles, vacío si no existe
     */
    @Query("SELECT c FROM Competicion c " +
            "LEFT JOIN FETCH c.deporte " +
            "LEFT JOIN FETCH c.configuracion " +
            "LEFT JOIN FETCH c.creador " +
            "WHERE c.id = :id")
    Optional<Competicion> findByIdWithDetails(
            @Param("id") long id
    );

    /**
     * Obtiene de forma paginada las competiciones públicas de un deporte concreto.
     *
     * @param id       identificador del deporte
     * @param pageable parámetros de paginación y ordenación
     * @return página de competiciones públicas del deporte indicado
     */
    @Query("SELECT c FROM Competicion c " +
            "LEFT JOIN FETCH c.deporte " +
            "LEFT JOIN FETCH c.creador " +
            "WHERE c.deporte.id = :deporteId AND c.publica = true")
    Page<Competicion> findByDeporteIdAndPublicaTrue(
            @Param("deporteId") long id,
            Pageable pageable
    );

    /**
     * Busca competiciones públicas cuyo nombre o descripción contengan el texto
     * indicado (búsqueda insensible a mayúsculas).
     *
     * @param search   texto a buscar en nombre o descripción
     * @param pageable parámetros de paginación y ordenación
     * @return página de competiciones públicas que coinciden con la búsqueda
     */
    @Query("SELECT c FROM Competicion c " +
            "LEFT JOIN FETCH c.deporte " +
            "LEFT JOIN FETCH c.creador " +
            "WHERE (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND c.publica = true")
    Page<Competicion> searchPublicas(
            @Param("search") String search,
            Pageable pageable
    );
}
