package com.hackaton.simulacaocredito.dtos.responses;

import java.time.LocalDate;
import java.util.List;

public record SimulacoesProdutosResponseDto(
        LocalDate dataReferencia,
        List<SimulacaoProdutoItemDto> simulacoes
) {}