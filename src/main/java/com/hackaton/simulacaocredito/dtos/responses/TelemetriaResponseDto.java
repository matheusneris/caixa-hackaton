package com.hackaton.simulacaocredito.dtos.responses;

import java.time.LocalDate;
import java.util.List;

public record TelemetriaResponseDto(
         LocalDate dataReferencia,
         List<TelemetriaItemDto> listaEndpoints
) {}