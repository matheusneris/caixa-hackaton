package com.hackaton.simulacaocredito.dtos.responses;

import java.math.BigDecimal;

public record ParcelaDto(
        Integer numero,
        BigDecimal valorAmortizacao,
        BigDecimal valorJuros,
        BigDecimal valorPrestacao
) {}