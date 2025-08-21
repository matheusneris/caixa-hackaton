package com.hackaton.simulacaocredito.dtos.responses;

import java.math.BigDecimal;

public record SimulacaoProdutoItemDto(
        Long codigoProduto,
        String descricaoProduto,
        BigDecimal taxaMediaJuro,
        BigDecimal valorMedioPrestacao,
        BigDecimal valorTotalDesejado,
        BigDecimal valorTotalCredito
) {}