package com.hackaton.simulacaocredito.dtos.responses;

import java.util.List;

public record ResultadoSimulacaoDto(
        String tipo,
        List<ParcelaDto> parcelas
) {}