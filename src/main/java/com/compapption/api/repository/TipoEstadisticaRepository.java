package com.compapption.api.repository;

import com.compapption.api.entity.TipoEstadistica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad TipoEstadistica.
 * Gestiona la consulta de tipos de estadística por deporte, filtrando
 * por estado activo y ordenando por campo de orden ascendente.
 *
 * @author Mario
 */
@Repository
public interface TipoEstadisticaRepository extends JpaRepository<TipoEstadistica, Long> {

    /**
     * Obtiene los tipos de estadística activos de un deporte, ordenados por el campo
     * {@code orden} de forma ascendente.
     *
     * @param deporteId identificador del deporte
     * @return lista de tipos de estadística activos del deporte, ordenados
     */
    List<TipoEstadistica> findByDeporteIdAndActivoTrueOrderByOrdenAsc(long deporteId);

    /**
     * Busca un tipo de estadística por deporte y nombre.
     *
     * @param deporteId identificador del deporte
     * @param nombre    nombre del tipo de estadística
     * @return Optional con el tipo de estadística, vacío si no existe
     */
    Optional<TipoEstadistica> findByDeporteIdAndNombre(long deporteId, String nombre);

    /**
     * Comprueba si ya existe un tipo de estadística con ese nombre en el deporte indicado.
     *
     * @param deporteId identificador del deporte
     * @param nombre    nombre del tipo de estadística
     * @return {@code true} si el nombre ya está en uso en ese deporte
     */
    boolean existsByDeporteIdAndNombre(long deporteId, String nombre);
}
