package com.compapption.api.controller;

import com.compapption.api.dto.invitacionDTO.InvitacionDetalleDTO;
import com.compapption.api.dto.invitacionDTO.InvitacionSimpleDTO;
import com.compapption.api.request.invitacion.InvitacionCreateRequest;
import com.compapption.api.service.InvitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invitaciones")
@RequiredArgsConstructor
public class InvitacionController {

    private final InvitacionService invitacionService;

    @PostMapping
    public ResponseEntity<InvitacionDetalleDTO> crear(
            @Valid @RequestBody InvitacionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitacionService.crearInvitacion(userDetails.getId(), request));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerPendientes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(invitacionService.obtenerPendientes(userDetails.getId()));
    }

    @GetMapping("/enviadas")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerEnviadas(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(invitacionService.obtenerEnviadas(userDetails.getId()));
    }

    @GetMapping("/competicion/{competicionId}")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerPorCompeticion(
            @PathVariable Long competicionId) {
        return ResponseEntity.ok(invitacionService.obtenerPorCompeticion(competicionId));
    }

    @PutMapping("/{token}/aceptar")
    public ResponseEntity<InvitacionDetalleDTO> aceptar(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(invitacionService.aceptarPorToken(token, userDetails.getId()));
    }

    @PutMapping("/{token}/rechazar")
    public ResponseEntity<InvitacionDetalleDTO> rechazar(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(invitacionService.rechazarPorToken(token, userDetails.getId()));
    }
}
