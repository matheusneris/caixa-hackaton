package com.hackaton.simulacaocredito.dtos.requests;

import java.math.BigDecimal;

public record SimulacaoRequestDto(
        BigDecimal valorDesejado,
        Integer prazo
){}
