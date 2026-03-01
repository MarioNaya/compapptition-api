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

@Service
@RequiredArgsConstructor
public class DeporteService {

    private final DeporteRepository deporteRepository;
    private final TipoEstadisticaRepository tipoEstadisticaRepository;
    private final DeporteMapper deporteMapper;
    private final TipoEstadisticaMapper tipoEstadisticaMapper;

    // === OBTENER LISTAS DEPORTE POR ACTIVO CON JERAQUÍA DETALLE === //

    @Transactional(readOnly = true)
    public List<DeporteSimpleDTO> obtenerTodosActivosSimple() {
        return deporteRepository.findByActivoTrue().stream()
                .map(deporteMapper::toSimpleDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeporteDetalleDTO> obtenerTodosActivosDetalle() {
        return deporteRepository.findByActivoTrue().stream()
                .map(deporteMapper::toDetalleDTO)
                .toList();
    }

    // === OBTENER DEPORTE POR ID === //

    @Transactional(readOnly = true)
    public DeporteSimpleDTO obtenerPorIdSimple(Long id){
        Deporte deporte = deporteRepository.findByIdWithEstadisticas(id)
                .orElseThrow(()-> new ResourceNotFoundException("Deporte", "id", id));

        return deporteMapper.toSimpleDTO(deporte);
    }

    @Transactional(readOnly = true)
    public DeporteDetalleDTO obtenerPorIdDetalle(long id){
        Deporte deporte = deporteRepository.findByIdWithEstadisticas(id)
                .orElseThrow(()-> new ResourceNotFoundException("Deporte", "id", id));

        return deporteMapper.toDetalleDTO(deporte);
    }

    // === CREAR, ACTUALIZAR Y ELIMINAR DEPORTE === //

    @Transactional
    public DeporteDetalleDTO crear(DeporteRequest request) {
        Deporte deporte = Deporte.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();
        return deporteMapper.toDetalleDTO(deporteRepository.save(deporte));
    }

    @Transactional
    public DeporteDetalleDTO actualizar(Long id, DeporteRequest request) {
        Deporte deporte = deporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deporte", "id", id));

        if (request.getNombre() != null) deporte.setNombre(request.getNombre());
        if (request.getDescripcion() != null) deporte.setDescripcion(request.getDescripcion());
        if (request.getActivo() != null) deporte.setActivo(request.getActivo());

        return deporteMapper.toDetalleDTO(deporteRepository.save(deporte));
    }

    @Transactional
    public void eliminar(Long id) {
        Deporte deporte = deporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deporte", "id", id));
        deporteRepository.delete(deporte);
    }

    // === OBTENER ESTADÍSTICAS DEPORTE === //

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
