package com.compapption.api.repository;

import com.compapption.api.entity.Rol;
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
            "LEFT JOIN FETCH urc.competicion " +
            "WHERE urc.usuario.id = :usuarioId")
    List<UsuarioRolCompeticion> findByUsuarioIdWithRolesAndCompeticiones(@Param("usuarioId") long usuarioId);

    @Query("SELECT urc FROM UsuarioRolCompeticion urc " +
            "LEFT JOIN FETCH urc.rol " +
            "WHERE urc.usuario.id = :usuarioId AND urc.competicion.id = :competicionId")
    List<UsuarioRolCompeticion> findByUsuarioIdAndCompeticionId(
            @Param("usuarioId") long usuarioId,
            @Param("competicionId") long competicionId
    );

    @Query("SELECT urc FROM UsuarioRolCompeticion urc " +
            "LEFT JOIN FETCH urc.usuario " +
            "LEFT JOIN FETCH urc.rol " +
            "WHERE urc.competicion.id = :competicionId")
    List<UsuarioRolCompeticion> findByCompeticionId(
            @Param("competicionId") long competicionId
    );

    @Query("SELECT urc FROM UsuarioRolCompeticion urc " +
            "LEFT JOIN FETCH urc.usuario " +
            "LEFT JOIN FETCH urc.rol " +
            "WHERE urc.competicion.id = :competicionId")
    List<UsuarioRolCompeticion> findByCompeticionId(@Param("competicionId") Long competicionId);

    boolean existsByUsuarioIdAndCompeticionIdAndRolNombre(
            long usuarioId,
            long competicionId,
            Rol.RolNombre nombre
    );

    void deleteByUsuarioIdAndCompeticionIdAndRolNombre(
            long usuarioId,
            long competicionId,
            Rol.RolNombre nombre
    );
}
