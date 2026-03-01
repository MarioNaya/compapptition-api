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

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogModificacionRepository logRepo;
    private final LogAsyncWriter logAsyncWriter;
    private final LogMapper logMapper;

    /**
     * Registra una operación de auditoría de forma asíncrona.
     * El userId y la IP se extraen sincrónicamente del hilo actual (request thread),
     * luego la escritura se delega al LogAsyncWriter.
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

    @Transactional
    public void clearCompeticion(Long competicionId) {
        logRepo.clearCompeticionId(competicionId);
    }

    @Transactional(readOnly = true)
    public Page<LogDTO> obtenerPorCompeticion(Long competicionId, Pageable pageable) {
        return logRepo.findByCompeticionIdWithDetails(competicionId, pageable)
                .map(logMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<LogDTO> obtenerPorUsuario(Long usuarioId, Pageable pageable) {
        return logRepo.findByUsuarioIdWithDetails(usuarioId, pageable)
                .map(logMapper::toDTO);
    }

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
