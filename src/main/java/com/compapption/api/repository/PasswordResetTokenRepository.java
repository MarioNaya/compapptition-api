package com.compapption.api.repository;

import com.compapption.api.entity.PasswordResetToken;
import com.compapption.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad PasswordResetToken.
 * Gestiona los tokens de recuperación de contraseña con expiración de 24 horas,
 * incluyendo la búsqueda por token y la eliminación de tokens expirados o por usuario.
 *
 * @author Mario
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token de recuperación de contraseña por su valor.
     *
     * @param token valor del token de recuperación
     * @return Optional con el token, vacío si no existe
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Elimina todos los tokens de recuperación de contraseña de un usuario.
     * Usado antes de generar un nuevo token para evitar duplicados.
     *
     * @param usuario entidad del usuario cuyos tokens se eliminan
     */
    @Modifying
    void deleteByUsuario(Usuario usuario);

    /**
     * Elimina todos los tokens cuya fecha de expiración sea anterior al instante indicado.
     * Usado en tareas de limpieza programada.
     *
     * @param fecha instante de referencia; se eliminan los tokens expirados antes de esa fecha
     */
    @Modifying
    void deleteByFechaExpiracionBefore(LocalDateTime fecha);
}
