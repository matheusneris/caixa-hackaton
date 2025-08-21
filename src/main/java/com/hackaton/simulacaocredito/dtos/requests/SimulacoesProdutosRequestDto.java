package com.hackaton.simulacaocredito.dtos.requests;

import java.time.LocalDate;

public record SimulacoesProdutosRequestDto(
        Long coProduto,
        LocalDate dataSimulacao
) {}