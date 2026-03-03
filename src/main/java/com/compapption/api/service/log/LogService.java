package com.compapption.api.service.log;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.log.LogDTO;
import com.compapption.api.entity.LogModificacion;
import com.compapption.api.mapper.LogMapper;
import com.compapption.api.repository.LogModificacionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * Servicio de auditoría que registra las operaciones realizadas sobre las entidades del sistema.
 * <p>
 * El método {@link #registrar} extrae de forma síncrona el usuario autenticado y la IP
 * del hilo de la petición HTTP (ya que el {@code SecurityContext} no es accesible desde
 * hilos asíncronos) y delega la escritura en base de datos a {@link LogAsyncWriter},
 * que la ejecuta en un hilo separado con {@code @Async}. Esto evita que los fallos de
 * auditoría bloqueen el flujo principal de la petición.
 * </p>
 * <p>
 * Importante: antes de eliminar una competición, se debe invocar
 * {@link #clearCompeticion(Long)} para desactivar la FK en los logs existentes y
 * evitar violaciones de integridad referencial.
 * </p>
 *
 * @author Mario
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogModificacionRepository logRepo;
    private final LogAsyncWriter logAsyncWriter;
    private final LogMapper logMapper;

    /**
     * Registra de forma asíncrona una operación de auditoría sobre una entidad del sistema.
     * <p>
     * Extrae el identificador de usuario y la IP de la petición actual de forma síncrona
     * (en el hilo del request) y delega la persistencia a {@link LogAsyncWriter#escribir}.
     * Si no hay usuario autenticado en el contexto, el registro se omite con una advertencia.
     * Los errores inesperados se capturan para no propagar excepciones al flujo principal.
     * </p>
     *
     * @param entidad         nombre de la entidad afectada (p.ej. {@code "Competicion"})
     * @param entidadId       identificador del registro afectado
     * @param accion          tipo de operación realizada ({@code CREAR}, {@code EDITAR}, {@code ELIMINAR})
     * @param datosAnteriores representación del estado anterior en formato JSON o texto (puede ser {@code null})
     * @param datosNuevos     representación del estado nuevo en formato JSON o texto (puede ser {@code null})
     * @param competicionId   identificador de la competición en cuyo contexto ocurrió la acción
     *                        (puede ser {@code null} para operaciones globales)
     */
    public void registrar(String entidad, Long entidadId, LogModificacion.AccionLog accion,
                          String datosAnteriores, String datosNuevos, Long competicionId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
                log.warn("No se pudo registrar log de auditoría: sin usuario autenticado");
                return;
            }

            Long userId = user.getId();
            String ip = extraerIp();

            logAsyncWriter.escribir(userId, competicionId, entidad, entidadId,
                    accion, datosAnteriores, datosNuevos, ip);

        } catch (Exception e) {
            log.error("Error al registrar log de auditoría para {} id={}: {}", entidad,entidadId, e.getMessage());
        }
    }

    /**
     * Desvincula todos los registros de log asociados a una competición poniendo su FK a {@code null}.
     * <p>
     * Debe invocarse antes de eliminar una competición para evitar violaciones de
     * integridad referencial en la tabla {@code log_modificacion}.
     * </p>
     *
     * @param competicionId identificador de la competición cuyos logs se van a desvincular
     */
    @Transactional
    public void clearCompeticion(Long competicionId) {
        logRepo.clearCompeticionId(competicionId);
    }

    /**
     * Devuelve de forma paginada los registros de auditoría de una competición.
     *
     * @param competicionId identificador de la competición
     * @param pageable      configuración de paginación y ordenación
     * @return página de {@link LogDTO} correspondiente a la competición
     */
    @Transactional(readOnly = true)
    public Page<LogDTO> obtenerPorCompeticion(Long competicionId, Pageable pageable) {
        return logRepo.findByCompeticionIdWithDetails(competicionId, pageable)
                .map(logMapper::toDTO);
    }

    /**
     * Devuelve de forma paginada los registros de auditoría generados por un usuario concreto.
     *
     * @param usuarioId identificador del usuario autor de las operaciones
     * @param pageable  configuración de paginación y ordenación
     * @return página de {@link LogDTO} del usuario indicado
     */
    @Transactional(readOnly = true)
    public Page<LogDTO> obtenerPorUsuario(Long usuarioId, Pageable pageable) {
        return logRepo.findByUsuarioIdWithDetails(usuarioId, pageable)
                .map(logMapper::toDTO);
    }

    /**
     * Devuelve todos los registros de auditoría asociados a un registro concreto de una entidad.
     *
     * @param entidad   nombre de la entidad (p.ej. {@code "Equipo"})
     * @param entidadId identificador del registro concreto de esa entidad
     * @return lista de {@link LogDTO} ordenada según la configuración del repositorio
     */
    @Transactional(readOnly = true)
    public List<LogDTO> obtenerPorEntidad(String entidad, Long entidadId) {
        return logRepo.findByEntidadAndEntidadIdWithDetails(entidad, entidadId)
                .stream()
                .map(logMapper::toDTO)
                .toList();
    }

    private String extraerIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return "desconocida";

            HttpServletRequest request = attributes.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "desconocida";
        }
    }
}
