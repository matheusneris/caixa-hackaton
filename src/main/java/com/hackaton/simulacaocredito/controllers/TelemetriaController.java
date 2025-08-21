package com.hackaton.simulacaocredito.controllers;

import com.hackaton.simulacaocredito.dtos.responses.TelemetriaResponseDto;
import com.hackaton.simulacaocredito.services.TelemetriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/telemetria")
public class TelemetriaController {

    private final TelemetriaService telemetriaService;

    public TelemetriaController(TelemetriaService telemetriaService) {
        this.telemetriaService = telemetriaService;
    }

    @GetMapping("/por-data")
    public ResponseEntity<TelemetriaResponseDto> listarPorData(@RequestParam LocalDate data) {
        return ResponseEntity.ok(telemetriaService.listarPorData(data));
    }
}
