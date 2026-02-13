package com.compapption.api.controller;

import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.service.ClasificacionService;
import com.compapption.api.service.CompeticionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/competiciones")
@RequiredArgsConstructor
public class CompeticionController {

    private final CompeticionService competicionService;
    private final ClasificacionService clasificacionService;

    // Obtener lista paginada en formato simple de competiciones públicas
    @GetMapping("/publicas")
    public ResponseEntity<PageResponse<CompeticionSimpleDTO>> listarPublicas(
            @PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(competicionService.obtenerPublicas(pageable));
    }

    // Obtener lista paginada en formato simple de competiciones buscando por nombre
    @GetMapping("/publicas/buscar")
    public ResponseEntity<PageResponse<CompeticionSimpleDTO>> buscarPublicas(
            @RequestParam String search,
            @PageableDefault(size = 20) Pageable pageable){
        return ResponseEntity.ok(competicionService.buscarPublicas(search, pageable));
    }

}
