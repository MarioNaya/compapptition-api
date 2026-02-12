package com.compapption.api.repository;

import com.compapption.api.entity.EquipoManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoManagerRepository extends JpaRepository<EquipoManager, Long> {

    Optional<EquipoManager> findByEquipoIdAndCompeticionIdAndUsuarioId(
            long equipoId,
            long competicionId,
            long usuarioId
    );

    @Query("SELECT em FROM EquipoManager em " +
            "LEFT JOIN FETCH em.usuario " +
            "WHERE em.equipo.id = :equipoId " +
            "AND em.competicion.id = :competicionId")
    List<EquipoManager> findByEquipoIdAndCompeticionId(
            @Param("equipoId") long equipoId,
            @Param("competicionId") long competicionId
    );

    @Query("SELECT em FROM EquipoManager em " +
            "LEFT JOIN FETCH em.equipo " +
            "LEFT JOIN FETCH em.competicion " +
            "WHERE em.usuario.id = :usuarioId")
    List<EquipoManager> findByUsuarioId(
            @Param("usuarioId") long usuarioId
    );

    boolean existsByEquipoIdAndCompeticionIdAndUsuarioId(
            long equipoId,
            long competicionId,
            long usuarioId
    );

    void deleteByEquipoIdAndCompeticionId(
            long equipoId,
            long competicionId
    );

}
