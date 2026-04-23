package com.compapption.api.repository;

import com.compapption.api.entity.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Mensaje}. Ofrece consultas paginadas, el conteo
 * de mensajes no leídos del otro usuario y la marca masiva de lectura.
 *
 * @author Mario
 */
@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /**
     * Devuelve los mensajes de una conversación en páginas, ordenados por fecha descendente
     * (más recientes primero). Incluye el autor en fetch para evitar N+1 al mapear a DTO.
     *
     * @param conversacionId identificador de la conversación
     * @param pageable       configuración de paginación y orden
     * @return página de mensajes
     */
    @Query(value = "SELECT m FROM Mensaje m " +
            "LEFT JOIN FETCH m.autor " +
            "WHERE m.conversacion.id = :conversacionId " +
            "ORDER BY m.fechaEnvio DESC",
            countQuery = "SELECT COUNT(m) FROM Mensaje m WHERE m.conversacion.id = :conversacionId")
    Page<Mensaje> findByConversacionIdOrderByFechaEnvioDesc(
            @Param("conversacionId") Long conversacionId,
            Pageable pageable);

    /**
     * Cuenta los mensajes no leídos (con {@code leidoAt = null}) de una conversación cuyo
     * autor sea distinto del usuario indicado (es decir, los que ese usuario debería leer).
     *
     * @param conversacionId identificador de la conversación
     * @param autorId        identificador del usuario cuyo propio autor se excluye del conteo
     * @return cantidad de mensajes pendientes de lectura
     */
    long countByConversacionIdAndAutorIdNotAndLeidoAtIsNull(Long conversacionId, Long autorId);

    /**
     * Obtiene el mensaje más reciente de una conversación, útil para la previsualización
     * en la lista de conversaciones (limit 1 se controla desde el servicio).
     *
     * @param conversacionId identificador de la conversación
     * @return lista (normalmente de un elemento) con el último mensaje
     */
    @Query("SELECT m FROM Mensaje m " +
            "WHERE m.conversacion.id = :conversacionId " +
            "ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findTopByConversacionId(@Param("conversacionId") Long conversacionId, Pageable pageable);

    /**
     * Marca como leídos (fija {@code leidoAt}) todos los mensajes de una conversación cuyo
     * autor sea distinto del usuario lector y que aún no estuvieran leídos.
     *
     * @param conversacionId identificador de la conversación
     * @param usuarioId      usuario que está leyendo (se excluyen sus propios mensajes)
     * @param now            fecha/hora a fijar como {@code leidoAt}
     * @return número de mensajes actualizados
     */
    @Modifying
    @Query("UPDATE Mensaje m SET m.leidoAt = :now " +
            "WHERE m.conversacion.id = :conversacionId " +
            "AND m.autor.id <> :usuarioId " +
            "AND m.leidoAt IS NULL")
    int marcarComoLeidos(@Param("conversacionId") Long conversacionId,
                         @Param("usuarioId") Long usuarioId,
                         @Param("now") LocalDateTime now);
}
