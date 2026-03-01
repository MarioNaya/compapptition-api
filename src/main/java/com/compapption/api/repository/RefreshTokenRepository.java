package com.compapption.api.repository;

import com.compapption.api.entity.RefreshToken;
import com.compapption.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("SELECT rt FROM RefreshToken rt LEFT JOIN FETCH rt.usuario WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUsuario(@Param("token") String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revocado = true WHERE rt.usuario = :usuario AND rt.revocado = false")
    void revocarTodosPorUsuario(@Param("usuario") Usuario usuario);

    List<RefreshToken> findByUsuarioId(Long usuarioId);
}