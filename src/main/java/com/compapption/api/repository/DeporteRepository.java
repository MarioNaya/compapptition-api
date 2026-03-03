package com.compapption.api.repository;

import com.compapption.api.entity.Deporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Deporte.
 * Gestiona la búsqueda de deportes por nombre y estado activo,
 * así como la carga eager de los tipos de estadística asociados.
 *
 * @author Mario
 */
@Repository
public interface DeporteRepository extends JpaRepository<Deporte, Long> {

    /**
     * Busca un deporte por su nombre exacto.
     *
     * @param nombre nombre del deporte
     * @return Optional con el deporte, vacío si no existe
     */
    Optional<Deporte> findByNombre(String nombre);

    /**
     * Obtiene todos los deportes marcados como activos.
     *
     * @return lista de deportes activos
     */
    List<Deporte> findByActivoTrue();

    /**
     * Busca un deporte por su identificador cargando en la misma consulta
     * el conjunto de tipos de estadística asociados (evita N+1).
     *
     * @param id identificador del deporte
     * @return Optional con el deporte y sus tipos de estadística, vacío si no existe
     */
    @Query("SELECT d FROM Deporte d " +
            "LEFT JOIN FETCH d.tipoEstadisticaSet " +
            "WHERE d.id = :id")
    Optional<Deporte> findByIdWithEstadisticas(
            @Param("id") long id
    );

    /**
     * Comprueba si ya existe un deporte con el nombre indicado.
     *
     * @param nombre nombre del deporte a verificar
     * @return {@code true} si el nombre ya está en uso
     */
    boolean existsByNombre(String nombre);
}
