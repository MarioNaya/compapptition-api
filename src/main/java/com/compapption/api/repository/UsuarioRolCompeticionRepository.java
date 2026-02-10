package com.compapption.api.repository;

import com.compapption.api.entity.UsuarioRolCompeticion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRolCompeticionRepository extends JpaRepository<UsuarioRolCompeticion, Long> {

    @Query("SELECT urc FROM UsuarioRolCompeticion urc " +
            "LEFT JOIN FETCH urc.rol " +
            "WHERE urc.usuario.id = :usuarioId AND urc.competicion.id = :competicionId")
    List<UsuarioRolCompeticion> findByUsuarioIdAndCompeticionId(
            @Param("usuarioId") Long usuarioId,
            @Param("competicionId") Long competicionId
    );

    @Query("SELECT urc FROM UsuarioRolCompeticion urc " +
            "LEFT JOIN FETCH urc.usuario " +
            "LEFT JOIN FETCH urc.rol " +
            "WHERE urc.competicion.id = :competicionId")
    List<UsuarioRolCompeticion> findByCompeticionId(
            @Param("competicionId") Long competicionId
    );

    Optional<UsuarioRolCompeticion> findByUsuarioIdAndCompeticionIdAndRolId(
            Long usuarioId,
            Long competicionId,
            Long rolId
    );

    boolean existsByUsuarioIdAndCompeticionIdAndRolId(
            Long usuarioId,
            Long competicionId,
            Long rolId
    );

    void deleteByUsuarioIdAndCompeticionIdAndRolId(
            Long usuarioId,
            Long competicionId,
            Long rolId
    );
}
