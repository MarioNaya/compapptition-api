package com.compapption.api.service;

import com.compapption.api.dto.clasificacionDTO.ClasificacionDetalleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionSimpleDTO;
import com.compapption.api.entity.Clasificacion;
import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Equipo;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.ClasificacionMapper;
import com.compapption.api.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClasificacionService {

    private final ClasificacionRepository clasificacionRepository;
    private final CompeticionRepository competicionRepository;
    private final EventoRepository eventoRepository;
    private final EventoEquipoRepository eventoEquipoRepository;
    private final ConfiguracionCompeticionRepository configuracionCompeticionRepository;
    private final ClasificacionMapper clasificacionMapper;

    @Transactional(readOnly = true)
    public List<ClasificacionDetalleDTO> obtenerClasificacionDetalle(Long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return clasificacionMapper.toDetalleDTOList(
                clasificacionRepository.findByCompeticionId(competicionId)
        );
    }

    @Transactional(readOnly = true)
    public List<ClasificacionSimpleDTO> obtenerClasificacionSimple(Long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }
        return clasificacionMapper.toSimpleDTOList(
                clasificacionRepository.findByCompeticionId(competicionId)
        );
    }

    @Transactional
    public void inicializarClasificacionEquipo(Competicion competicion, Equipo equipo){
        if (clasificacionRepository.findByCompeticionIdAndEquipoId(competicion.getId(), equipo.getId()).isEmpty()){
            Clasificacion clasificacion = Clasificacion.builder()
                    .competicion(competicion)
                    .equipo(equipo)
                    .posicion(0)
                    .puntos(0)
                    .partidosJugados(0)
                    .victorias(0)
                    .empates(0)
                    .derrotas(0)
                    .golesFavor(0)
                    .golesContra(0)
                    .diferenciaGoles(0)
                    .build();

            clasificacionRepository.save(clasificacion);

        }
    }
}
