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

/**
 * Repositorio JPA para la entidad RefreshToken.
 * Gestiona los tokens de refresco JWT, incluyendo la búsqueda por valor de token,
 * la revocación masiva por usuario y la consulta de tokens activos.
 *
 * @author Mario
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un refresh token por su valor cargando el usuario asociado en la misma consulta.
     * Usado al validar el token en el endpoint de refresco.
     *
     * @param token valor del refresh token
     * @return Optional con el token y su usuario, vacío si no existe
     */
    @Query("SELECT rt FROM RefreshToken rt LEFT JOIN FETCH rt.usuario WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUsuario(@Param("token") String token);

    /**
     * Marca como revocados todos los refresh tokens activos de un usuario.
     * Usado al hacer logout o al rotar tokens.
     *
     * @param usuario entidad del usuario cuyos tokens se revocan
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revocado = true WHERE rt.usuario = :usuario AND rt.revocado = false")
    void revocarTodosPorUsuario(@Param("usuario") Usuario usuario);

    /**
     * Obtiene todos los refresh tokens de un usuario (incluyendo revocados).
     *
     * @param usuarioId identificador del usuario
     * @return lista de refresh tokens del usuario
     */
    List<RefreshToken> findByUsuarioId(Long usuarioId);
}