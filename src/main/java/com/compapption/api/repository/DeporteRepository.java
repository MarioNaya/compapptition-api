package com.compapption.api.repository;

import com.compapption.api.entity.Deporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeporteRepository extends JpaRepository<Deporte, Long> {

    Optional<Deporte> findByNombre(String nombre);

    List<Deporte> findByActivoTrue();

    @Query("SELECT d FROM Deporte d " +
            "LEFT JOIN FETCH d.tipoEstadisticaSet " +
            "WHERE d.id = :id")
    Optional<Deporte> findByIdWithEstadisticas(
            @Param("id") Long id
    );

    boolean existsByNombre(String nombre);
}
