package com.compapption.api.service;

import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.entity.Competicion;
import com.compapption.api.mapper.CompeticionMapper;
import com.compapption.api.mapper.EquipoMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CompeticionService {

    private final CompeticionRepository competicionRepository;
    private final DeporteRepository desporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompeticionEquipoRepository competicionEquipoRepository;
    private final EquipoRepository equipoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    private final CompeticionMapper competicionMapper;
    private final EquipoMapper equipoMapper;
    private final ClasificacionService clasificacionService;

    public PageResponse<CompeticionSimpleDTO> obtenerPublicas(Pageable pageable){
        Page<Competicion> page = competicionRepository.findByPublicasActivas(pageable);
        return toPageResponseSimple(page);
    }

    private PageResponse<CompeticionSimpleDTO> toPageResponseSimple(Page<Competicion> page) {
        return PageResponse.<CompeticionSimpleDTO>builder()
                .content(competicionMapper.toSimpleDTOList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private PageResponse<CompeticionDetalleDTO> toPageResponseDetalle(Page<Competicion> page) {
        return PageResponse.<CompeticionDetalleDTO>builder()
                .content(competicionMapper.toDetalleDTOList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
