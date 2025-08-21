package com.hackaton.simulacaocredito.services;

import com.hackaton.simulacaocredito.dtos.responses.TelemetriaItemDto;
import com.hackaton.simulacaocredito.dtos.responses.TelemetriaResponseDto;
import com.hackaton.simulacaocredito.models.Telemetria;
import com.hackaton.simulacaocredito.repositories.TelemetriaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service 
public class TelemetriaService { 

    private final TelemetriaRepository repository; 

    public TelemetriaService(TelemetriaRepository repository) { 
        this.repository = repository; 
    }

    @Transactional 
    public void registrar(String nomeApi, long tempoMs, int status) { 
        Telemetria registro = new Telemetria(); 
        registro.setNomeApi(nomeApi); 
        registro.setTempoResposta(tempoMs); 
        registro.setStatus(status); 
        registro.setDataReferencia(LocalDate.now()); 
        repository.save(registro); 
    } 

    @Transactional(readOnly = true) 
    public TelemetriaResponseDto listarPorData(LocalDate data) { 
        List<Telemetria> registros = repository.findByDataReferencia(data); 

        Map<String, List<Telemetria>> grouped = registros.stream() 
                .collect(Collectors.groupingBy(Telemetria::getNomeApi)); 

        List<TelemetriaItemDto> lista = grouped.entrySet().stream() 
                .map(entry -> { 
                    List<Telemetria> itens = entry.getValue(); 
                    long qtd = itens.size(); 
                    long tempoMin = itens.stream().mapToLong(Telemetria::getTempoResposta).min().orElse(0); 
                    long tempoMax = itens.stream().mapToLong(Telemetria::getTempoResposta).max().orElse(0); 
                    BigDecimal tempoMedio = BigDecimal.valueOf(
                            itens.stream().mapToLong(Telemetria::getTempoResposta).sum()
                    ).divide(BigDecimal.valueOf(qtd), 2, RoundingMode.HALF_UP);
                    BigDecimal percentualSucesso = BigDecimal.valueOf( 
                            itens.stream().filter(t -> t.getStatus() == 200).count() 
                    ).divide(BigDecimal.valueOf(qtd), 2, RoundingMode.HALF_UP); 

                    return new TelemetriaItemDto(entry.getKey(), qtd, tempoMedio, tempoMin, tempoMax, percentualSucesso); 
                }) 
                .toList(); 

        return new TelemetriaResponseDto(data, lista); 
    } 
}
