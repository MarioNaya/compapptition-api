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

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAsyncWriter {

    private final LogModificacionRepository logRepo;
    private final UsuarioRepository usuarioRepository;
    private final CompeticionRepository competicionRepository;

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
