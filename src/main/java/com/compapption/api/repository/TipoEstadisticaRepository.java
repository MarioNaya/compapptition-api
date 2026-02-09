package com.compapption.api.repository;

import com.compapption.api.entity.TipoEstadistica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoEstadisticaRepository extends JpaRepository<TipoEstadistica, Long> {

    List<TipoEstadistica> findByDeporteIdAndActivoTrueOrderByOrdenAsc(Long deporteId);

    Optional<TipoEstadistica> findByDeporteIdAndNombre(Long deporteId, String nombre);

    boolean existsByDeporteIdAndNombre(Long deporteId, String nombre);
}
