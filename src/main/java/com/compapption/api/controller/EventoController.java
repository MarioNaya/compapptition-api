package com.compapption.api.controller;

import com.compapption.api.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/competiciones({competicionId}/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;


}
