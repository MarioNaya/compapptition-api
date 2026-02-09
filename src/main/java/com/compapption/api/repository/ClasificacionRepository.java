package com.compapption.api.repository;

import com.compapption.api.entity.Clasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Long> {

    @Query("SELECT c FROM Clasificacion c " +
            "LEFT JOIN FETCH c.equipo " +
            "WHERE c.competicion.id = :competicionId " +
            "ORDER BY c.posicion ASC")
    List<Clasificacion> findByCompeticionIdOrderByPosicion(
            @Param("competicionId") Long competicionId
    );

    Optional<Clasificacion> findByCompeticionIdAndEquipoId(Long competicionId, Long equipoId);

    @Query("SELECT c FROM Clasificacion c " +
            "WHERE c.competicion.id = :competicionId")
    List<Clasificacion> findByCompeticionId(
            @Param("competicionId") Long competicionId
    );

    void deleteByCompeticionId(Long competicionId);

    void deleteByCompeticionIdAndEquipoId(Long competicionId, Long equipoId);
}
