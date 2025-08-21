package com.hackaton.simulacaocredito.dtos.responses;

import java.math.BigDecimal;
import java.util.List;

public record SimulacaoResponseDto(
        Long idSimulacao,
        Long codigoProduto,
        String descricaoProduto,
        BigDecimal taxaJuros,
        List<ResultadoSimulacaoDto> resultadoSimulacao
) {}