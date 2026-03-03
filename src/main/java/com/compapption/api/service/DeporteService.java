package com.compapption.api.service;

import com.compapption.api.dto.deporteDTO.DeporteDetalleDTO;
import com.compapption.api.dto.deporteDTO.DeporteSimpleDTO;
import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import com.compapption.api.entity.Deporte;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.request.deporte.DeporteRequest;
import com.compapption.api.mapper.DeporteMapper;
import com.compapption.api.mapper.TipoEstadisticaMapper;
import com.compapption.api.repository.DeporteRepository;
import com.compapption.api.repository.TipoEstadisticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de gestión de deportes.
 *
 * <p>Cubre el ciclo de vida completo de un deporte: listado de deportes activos
 * (simple y detalle), consulta por id, creación, actualización, eliminación y
 * recuperación de los tipos de estadística asociados a un deporte concreto.</p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class DeporteService {

    private final DeporteRepository deporteRepository;
    private final TipoEstadisticaRepository tipoEstadisticaRepository;
    private final DeporteMapper deporteMapper;
    private final TipoEstadisticaMapper tipoEstadisticaMapper;

    // === OBTENER LISTAS DEPORTE POR ACTIVO CON JERAQUÍA DETALLE === //

    /**
     * Devuelve todos los deportes activos en formato simple.
     *
     * @return lista de {@link DeporteSimpleDTO} con los deportes activos
     */
    @Transactional(readOnly = true)
    public List<DeporteSimpleDTO> obtenerTodosActivosSimple() {
        return deporteRepository.findByActivoTrue().stream()
                .map(deporteMapper::toSimpleDTO)
                .toList();
    }

    /**
     * Devuelve todos los deportes activos en formato detalle (incluye tipos de estadística).
     *
     * @return lista de {@link DeporteDetalleDTO} con los deportes activos
     */
    @Transactional(readOnly = true)
    public List<DeporteDetalleDTO> obtenerTodosActivosDetalle() {
        return deporteRepository.findByActivoTrue().stream()
                .map(deporteMapper::toDetalleDTO)
                .toList();
    }

    // === OBTENER DEPORTE POR ID === //

    /**
     * Obtiene un deporte en formato simple por su identificador (carga estadísticas asociadas).
     *
     * @param id identificador del deporte
     * @return {@link DeporteSimpleDTO} con los campos básicos del deporte
     * @throws ResourceNotFoundException si no existe ningún deporte con ese id
     */
    @Transactional(readOnly = true)
    public DeporteSimpleDTO obtenerPorIdSimple(Long id){
        Deporte deporte = deporteRepository.findByIdWithEstadisticas(id)
                .orElseThrow(()-> new ResourceNotFoundException("Deporte", "id", id));

        return deporteMapper.toSimpleDTO(deporte);
    }

    /**
     * Obtiene un deporte en formato detalle por su identificador (carga estadísticas asociadas).
     *
     * @param id identificador del deporte
     * @return {@link DeporteDetalleDTO} con todos los datos del deporte y sus tipos de estadística
     * @throws ResourceNotFoundException si no existe ningún deporte con ese id
     */
    @Transactional(readOnly = true)
    public DeporteDetalleDTO obtenerPorIdDetalle(long id){
        Deporte deporte = deporteRepository.findByIdWithEstadisticas(id)
                .orElseThrow(()-> new ResourceNotFoundException("Deporte", "id", id));

        return deporteMapper.toDetalleDTO(deporte);
    }

    // === CREAR, ACTUALIZAR Y ELIMINAR DEPORTE === //

    /**
     * Crea un nuevo deporte con los datos proporcionados.
     *
     * <p>Si {@code activo} no se incluye en el request se establece a {@code true} por defecto.</p>
     *
     * @param request datos del deporte (nombre, descripción y activo opcional)
     * @return {@link DeporteDetalleDTO} con el deporte recién creado
     */
    @Transactional
    public DeporteDetalleDTO crear(DeporteRequest request) {
        Deporte deporte = Deporte.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();
        return deporteMapper.toDetalleDTO(deporteRepository.save(deporte));
    }

    /**
     * Actualiza los datos de un deporte existente.
     *
     * <p>Solo se modifican los campos no nulos del request.</p>
     *
     * @param id      identificador del deporte a actualizar
     * @param request campos a actualizar (nombre, descripción y/o activo)
     * @return {@link DeporteDetalleDTO} con los datos actualizados
     * @throws ResourceNotFoundException si no existe ningún deporte con ese id
     */
    @Transactional
    public DeporteDetalleDTO actualizar(Long id, DeporteRequest request) {
        Deporte deporte = deporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deporte", "id", id));

        if (request.getNombre() != null) deporte.setNombre(request.getNombre());
        if (request.getDescripcion() != null) deporte.setDescripcion(request.getDescripcion());
        if (request.getActivo() != null) deporte.setActivo(request.getActivo());

        return deporteMapper.toDetalleDTO(deporteRepository.save(deporte));
    }

    /**
     * Elimina un deporte del sistema.
     *
     * @param id identificador del deporte a eliminar
     * @throws ResourceNotFoundException si no existe ningún deporte con ese id
     */
    @Transactional
    public void eliminar(Long id) {
        Deporte deporte = deporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deporte", "id", id));
        deporteRepository.delete(deporte);
    }

    // === OBTENER ESTADÍSTICAS DEPORTE === //

    /**
     * Devuelve los tipos de estadística activos de un deporte ordenados por su campo {@code orden}.
     *
     * @param deporteId identificador del deporte
     * @return lista de {@link TipoEstadisticaDTO} activos asociados al deporte
     * @throws ResourceNotFoundException si no existe ningún deporte con ese id
     */
    @Transactional(readOnly = true)
    public List<TipoEstadisticaDTO> obtenerEstadisticasPorDeporte(long deporteId) {
        if (!deporteRepository.existsById(deporteId)){
            throw new ResourceNotFoundException("Deporte","id", deporteId);
        }

        return tipoEstadisticaRepository.findByDeporteIdAndActivoTrueOrderByOrdenAsc(deporteId)
                .stream()
                .map(tipoEstadisticaMapper::toTipoEstadisticaDTO)
                .toList();
    }
}
