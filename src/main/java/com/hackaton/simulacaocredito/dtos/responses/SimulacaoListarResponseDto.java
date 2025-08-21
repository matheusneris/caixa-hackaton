package com.hackaton.simulacaocredito.dtos.responses;

import java.util.List;

public record SimulacaoListarResponseDto(
        int pagina,
        long qtdRegistros,
        int qtdRegistrosPagina,
        List<RegistroListarDto> registros
) {}
