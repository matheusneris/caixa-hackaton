package com.hackaton.simulacaocredito.dtos.responses;

import java.math.BigDecimal;

public record RegistroListarDto(
        Long idSimulacao,
        BigDecimal valorDesejado,
        int prazo,
        BigDecimal valorTotalParcelas
) {}