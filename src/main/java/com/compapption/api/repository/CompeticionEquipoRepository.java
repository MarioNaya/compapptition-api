package com.compapption.api.repository;

import com.compapption.api.entity.CompeticionEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompeticionEquipoRepository extends JpaRepository<CompeticionEquipo, Long> {
    
    Optional<CompeticionEquipo> findByCompeticionIdAndEquipoId(Long competicionId, Long equipoId);
    
    @Query("SELECT ce FROM CompeticionEquipo ce " +
            "LEFT JOIN FETCH ce.equipo " +
            "WHERE ce.competicion.id = :competicionId AND ce.activo = true")
    List<CompeticionEquipo> findActivosByCompeticionId(
            @Param("competicionId") Long competicionId
    );
    
    @Query("SELECT ce FROM CompeticionEquipo ce " +
            "LEFT JOIN FETCH ce.competicion " +
            "WHERE ce.equipo.id = :equipoId AND ce.activo = true")
    List<CompeticionEquipo> findActivosByEquipoId(
            @Param("equipoId") Long equipoId
    );
    
    boolean existsByCompeticionIdAndEquipoId(Long competicionId, Long equipoId);

    @Query("SELECT COUNT(ce) FROM CompeticionEquipo ce " +
            "WHERE ce.competicion.id = :competicionId AND ce.activo = true")
    long countActivosByCompeticionId(
            @Param("competicionId") Long competicionId
    );
}
