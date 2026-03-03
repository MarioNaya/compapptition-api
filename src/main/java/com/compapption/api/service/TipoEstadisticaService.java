package com.compapption.api.service;

import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import com.compapption.api.entity.Deporte;
import com.compapption.api.entity.TipoEstadistica;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.TipoEstadisticaMapper;
import com.compapption.api.repository.DeporteRepository;
import com.compapption.api.repository.TipoEstadisticaRepository;
import com.compapption.api.request.tipoestadistica.TipoEstadisticaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de gestión de tipos de estadística por deporte.
 *
 * <p>Permite consultar, crear, actualizar y desactivar (soft-delete) los tipos de
 * estadística (p. ej. goles, asistencias, puntos) que se asocian a un deporte
 * concreto. Los tipos se ordenan por el campo {@code orden} y se filtran por
 * {@code activo = true} en las consultas de listado.</p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class TipoEstadisticaService {

    private final TipoEstadisticaRepository tipoEstadisticaRepository;
    private final TipoEstadisticaMapper tipoEstadisticaMapper;
    private final DeporteRepository deporteRepository;

    /**
     * Devuelve los tipos de estadística activos de un deporte, ordenados por su campo {@code orden}.
     *
     * @param deporteId identificador del deporte
     * @return lista de {@link TipoEstadisticaDTO} activos pertenecientes al deporte
     * @throws ResourceNotFoundException si no existe ningún deporte con ese id
     */
    @Transactional(readOnly = true)
    public List<TipoEstadisticaDTO> obtenerPorDeporte(Long deporteId) {
        if (!deporteRepository.existsById(deporteId)) {
            throw new ResourceNotFoundException("Deporte", "id", deporteId);
        }
        return tipoEstadisticaMapper.toTipoEstadisticaDTOList(
                tipoEstadisticaRepository.findByDeporteIdAndActivoTrueOrderByOrdenAsc(deporteId));
    }

    /**
     * Obtiene un tipo de estadística por su identificador.
     *
     * @param id identificador del tipo de estadística
     * @return {@link TipoEstadisticaDTO} con los datos del tipo
     * @throws ResourceNotFoundException si no existe ningún tipo de estadística con ese id
     */
    @Transactional(readOnly = true)
    public TipoEstadisticaDTO obtenerPorId(Long id) {
        return tipoEstadisticaMapper.toTipoEstadisticaDTO(tipoEstadisticaRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Tipo estadística", "id", id)));
    }

    /**
     * Crea un nuevo tipo de estadística asociado a un deporte.
     *
     * <p>Verifica que el nombre del tipo no exista ya para el mismo deporte antes de persistir.</p>
     *
     * @param deporteId identificador del deporte al que se asocia el tipo
     * @param request   datos del tipo de estadística (nombre, descripción, tipoValor, orden)
     * @return {@link TipoEstadisticaDTO} con el tipo recién creado
     * @throws ResourceNotFoundException si no existe ningún deporte con ese id
     * @throws BadRequestException       si ya existe un tipo con ese nombre para el deporte
     */
    @Transactional
    public TipoEstadisticaDTO crear(Long deporteId, TipoEstadisticaRequest request) {
        Deporte deporte = deporteRepository.findById(deporteId)
                .orElseThrow(()-> new ResourceNotFoundException("Deporte", "id", deporteId));

        if (tipoEstadisticaRepository.existsByDeporteIdAndNombre(deporteId, request.getNombre())) {
            throw new BadRequestException("Ya existe un tipo de estadística con ese nombre para este deporte");
        }

        TipoEstadistica tipoEstadistica = TipoEstadistica.builder()
                .deporte(deporte)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tipoValor(request.getTipoValor())
                .orden(request.getOrden())
                .build();

        return tipoEstadisticaMapper.toTipoEstadisticaDTO(tipoEstadisticaRepository.save(tipoEstadistica));
    }

    /**
     * Actualiza los datos de un tipo de estadística existente.
     *
     * <p>Solo se modifican los campos no nulos del request. Si se cambia el nombre,
     * se verifica que el nuevo nombre no exista ya para el mismo deporte.</p>
     *
     * @param id      identificador del tipo de estadística a actualizar
     * @param request campos a actualizar (nombre, descripción, tipoValor y/o orden)
     * @return {@link TipoEstadisticaDTO} con los datos actualizados
     * @throws ResourceNotFoundException si no existe ningún tipo de estadística con ese id
     * @throws BadRequestException       si el nuevo nombre ya está en uso en el mismo deporte
     */
    @Transactional
    public TipoEstadisticaDTO actualizar(Long id, TipoEstadisticaRequest request) {
        TipoEstadistica tipo = tipoEstadisticaRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Tipo estadística", "id", id));

        if (request.getNombre() != null && !request.getNombre().equals(tipo.getNombre())) {
            if (tipoEstadisticaRepository.existsByDeporteIdAndNombre(
                    tipo.getDeporte().getId(), request.getNombre())) {
                throw new BadRequestException("Ya existe un tipo de estadística con ese nombre para este deporte");
            }
            tipo.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            tipo.setDescripcion(request.getDescripcion());
        }
        if (request.getTipoValor() != null) {
            tipo.setTipoValor(request.getTipoValor());
        }
        if (request.getOrden() != null) {
            tipo.setOrden(request.getOrden());
        }

        return tipoEstadisticaMapper.toTipoEstadisticaDTO(tipoEstadisticaRepository.save(tipo));
    }

    /**
     * Desactiva (soft-delete) un tipo de estadística marcándolo como inactivo.
     *
     * <p>El registro no se elimina físicamente de la base de datos para preservar la
     * integridad referencial con estadísticas históricas.</p>
     *
     * @param id identificador del tipo de estadística a desactivar
     * @throws ResourceNotFoundException si no existe ningún tipo de estadística con ese id
     */
    @Transactional
    public void eliminar(Long id) {
        TipoEstadistica tipo = tipoEstadisticaRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Tipo estadística", "id", id));
        tipo.setActivo(false);
        tipoEstadisticaRepository.save(tipo);
    }
}
