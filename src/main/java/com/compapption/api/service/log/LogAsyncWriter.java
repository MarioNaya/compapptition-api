package com.compapption.api.service.log;

import com.compapption.api.entity.LogModificacion;
import com.compapption.api.repository.CompeticionRepository;
import com.compapption.api.repository.LogModificacionRepository;
import com.compapption.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Escritor asíncrono de registros de auditoría en base de datos.
 * <p>
 * Este componente es el único responsable de persistir las entradas de
 * {@link com.compapption.api.entity.LogModificacion}. Se ejecuta en un hilo
 * del pool de tareas asíncronas ({@code @Async}) con su propia transacción
 * ({@code @Transactional}), de forma que los fallos de escritura no afectan
 * a la transacción principal. Los errores se registran en el log de aplicación
 * sin propagarse.
 * </p>
 * <p>
 * No debe ser invocado directamente desde los servicios de negocio;
 * el punto de entrada es {@link LogService#registrar}.
 * </p>
 *
 * @author Mario
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogAsyncWriter {

    private final LogModificacionRepository logRepo;
    private final UsuarioRepository usuarioRepository;
    private final CompeticionRepository competicionRepository;

    /**
     * Persiste un registro de auditoría en la tabla {@code log_modificacion} de forma asíncrona.
     * <p>
     * Construye la entidad {@link com.compapption.api.entity.LogModificacion} usando referencias
     * perezosas para usuario y competición (sin consultas adicionales) y la guarda. Cualquier
     * excepción se captura y registra en el log de aplicación sin relanzarse.
     * </p>
     *
     * @param usuarioId       identificador del usuario que realizó la operación
     * @param competicionId   identificador de la competición afectada; puede ser {@code null}
     * @param entidad         nombre de la entidad sobre la que se actuó (p.ej. {@code "Partido"})
     * @param entidadId       identificador del registro afectado
     * @param accion          tipo de acción realizada ({@code CREAR}, {@code EDITAR}, {@code ELIMINAR})
     * @param datosAnteriores estado anterior serializado; puede ser {@code null}
     * @param datosNuevos     estado nuevo serializado; puede ser {@code null}
     * @param ip              dirección IP del cliente que realizó la petición
     */
    @Async
    @Transactional
    public void escribir(Long usuarioId, Long competicionId, String entidad, Long entidadId,
                         LogModificacion.AccionLog accion, String datosAnteriores, String datosNuevos, String ip) {
        try {
            LogModificacion logEntry = LogModificacion.builder()
                    .usuario(usuarioRepository.getReferenceById(usuarioId))
                    .competicion(competicionId != null ? competicionRepository.getReferenceById(competicionId) : null)
                    .entidad(entidad)
                    .entidadId(entidadId)
                    .accion(accion)
                    .datosAnteriores(datosAnteriores)
                    .datosNuevos(datosNuevos)
                    .ipAddress(ip)
                    .build();
            logRepo.save(logEntry);
        } catch (Exception e) {
            log.error("Error escribiendo log async: entidad={} id={}: {}", entidad, entidadId, e.getMessage());
        }
    }
}
