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

@Service
@RequiredArgsConstructor
public class TipoEstadisticaService {

    private final TipoEstadisticaRepository tipoEstadisticaRepository;
    private final TipoEstadisticaMapper tipoEstadisticaMapper;
    private final DeporteRepository deporteRepository;

    @Transactional(readOnly = true)
    public List<TipoEstadisticaDTO> obtenerPorDeporte(Long deporteId) {
        if (!deporteRepository.existsById(deporteId)) {
            throw new ResourceNotFoundException("Deporte", "id", deporteId);
        }
        return tipoEstadisticaMapper.toTipoEstadisticaDTOList(
                tipoEstadisticaRepository.findByDeporteIdAndActivoTrueOrderByOrdenAsc(deporteId));
    }

    @Transactional(readOnly = true)
    public TipoEstadisticaDTO obtenerPorId(Long id) {
        return tipoEstadisticaMapper.toTipoEstadisticaDTO(tipoEstadisticaRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Tipo estadística", "id", id)));
    }

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

    @Transactional
    public void eliminar(Long id) {
        TipoEstadistica tipo = tipoEstadisticaRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Tipo estadística", "id", id));
        tipo.setActivo(false);
        tipoEstadisticaRepository.save(tipo);
    }
}
