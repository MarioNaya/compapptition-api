package com.compapption.api.repository;

import com.compapption.api.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Conversacion}. Permite localizar una conversación
 * por la pareja normalizada de usuarios (A, B) y listar las conversaciones de un usuario
 * ordenadas por la fecha del último mensaje (bandeja de entrada).
 *
 * @author Mario
 */
@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    /**
     * Devuelve la conversación entre los dos usuarios indicados, asumiendo que los ids
     * ya vienen normalizados (usuarioA es el de id menor).
     *
     * @param usuarioA usuario con id menor
     * @param usuarioB usuario con id mayor
     * @return la conversación si existe
     */
    Optional<Conversacion> findByUsuarioAAndUsuarioB(
            com.compapption.api.entity.Usuario usuarioA,
            com.compapption.api.entity.Usuario usuarioB);

    /**
     * Lista todas las conversaciones en las que participa el usuario indicado, ordenadas
     * por la fecha del último mensaje descendente (más recientes primero). Usa COALESCE
     * para priorizar {@code fechaUltimoMensaje}; si es {@code null}, cae en {@code fechaCreacion}.
     *
     * @param usuarioId identificador del usuario
     * @return lista de conversaciones ordenadas
     */
    @Query("SELECT c FROM Conversacion c " +
            "LEFT JOIN FETCH c.usuarioA " +
            "LEFT JOIN FETCH c.usuarioB " +
            "WHERE c.usuarioA.id = :usuarioId OR c.usuarioB.id = :usuarioId " +
            "ORDER BY COALESCE(c.fechaUltimoMensaje, c.fechaCreacion) DESC")
    List<Conversacion> findAllByUsuarioAIdOrUsuarioBIdOrderByFechaUltimoMensajeDesc(
            @Param("usuarioId") Long usuarioId);
}
