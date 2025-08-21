package com.hackaton.simulacaocredito.dtos.responses;

import java.math.BigDecimal;

public record TelemetriaItemDto(
        String nomeApi,
        Long qtdRequisicoes,
        BigDecimal tempoMedio,
        Long tempoMinimo,
        Long tempoMaximo,
        BigDecimal percentualSucesso
) {}