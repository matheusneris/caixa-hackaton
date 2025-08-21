package com.hackaton.simulacaocredito.controllers;

import com.hackaton.simulacaocredito.dtos.requests.SimulacaoRequestDto;
import com.hackaton.simulacaocredito.dtos.requests.SimulacoesProdutosRequestDto;
import com.hackaton.simulacaocredito.dtos.responses.SimulacaoResponseDto;
import com.hackaton.simulacaocredito.dtos.responses.SimulacoesProdutosResponseDto;
import com.hackaton.simulacaocredito.services.SimulacaoCreditoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hackaton.simulacaocredito.dtos.responses.SimulacaoListarResponseDto;

@RestController
@RequestMapping("/v1/simulacao")
public class SimuladorController {

    private final SimulacaoCreditoService simulacaoService;

    public SimuladorController(SimulacaoCreditoService simulacaoService) {
        this.simulacaoService = simulacaoService;
    }

    @PostMapping("/nova")
    public ResponseEntity<SimulacaoResponseDto> fazerSimulacao(@RequestBody SimulacaoRequestDto simulacaoRequest) {
        return ResponseEntity.ok(simulacaoService.fazerSimulacaoCredito(simulacaoRequest));
    }

    @GetMapping("/listar")
    public ResponseEntity<SimulacaoListarResponseDto> listarTodasSimulacoes(
            @RequestParam(defaultValue = "1") int pagina, @RequestParam(defaultValue = "200") int qtdRegistrosPagina) {
        return ResponseEntity.ok(simulacaoService.listarTodasSimulacoes(pagina, qtdRegistrosPagina));
    }

    @GetMapping("/listar-por-produtos")
    public ResponseEntity<SimulacoesProdutosResponseDto> listarSimulacoesPorProdutos(@RequestBody SimulacoesProdutosRequestDto request) {
        return ResponseEntity.ok(simulacaoService.listarSimulacoesPorProdutos(request));
    }
}
