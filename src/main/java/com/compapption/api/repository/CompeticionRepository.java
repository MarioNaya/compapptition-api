package com.compapption.api.repository;

import com.compapption.api.entity.Competicion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompeticionRepository extends JpaRepository<Competicion, Long> {

    @Query("SELECT c FROM Competicion c " +
            "WHERE c.publica = true AND c.estado = 'ACTIVA'")
    Page<Competicion> findByPublicasActivas(Pageable pageable);

    @Query("SELECT c FROM Competicion c " +
            "WHERE c.creador.id = :usuarioId")
    List<Competicion> findByCreadorId(
            @Param("usuarioId") Long usuarioId
    );

    @Query("SELECT DISTINCT c FROM Competicion c " +
            "JOIN c.usuariosRol ur " +
            "WHERE ur.usuario.id = :usuarioid")
    List<Competicion> findByUsuarioParticipante(
            @Param("usuarioId") Long usuarioId
    );

    @Query("SELECT c FROM Competicion c " +
            "LEFT JOIN FETCH c.equipos ce " +
            "LEFT JOIN FETCH ce.equipo " +
            "WHERE c.id = :id")
    Optional<Competicion> findByIdWithEquipos(
            @Param("id") Long id
    );

    @Query("SELECT c FROM Competicion c " +
            "LEFT JOIN FETCH c.deporte " +
            "LEFT JOIN FETCH c.configuracion " +
            "LEFT JOIN FETCH c.creador " +
            "WHERE c.id = :id")
    Optional<Competicion> findByIdWithDetails(
            @Param("id") Long id
    );

    @Query("SELECT c FROM Competicion c " +
            "LEFT JOIN FETCH c.deporte " +
            "LEFT JOIN FETCH c.creador " +
            "WHERE c.deporte.id = :deporteId AND c.publica = true")
    Page<Competicion> findByDeporteIdAndPublicaTrue(
            @Param("deporteId") Long id,
            Pageable pageable
    );

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
