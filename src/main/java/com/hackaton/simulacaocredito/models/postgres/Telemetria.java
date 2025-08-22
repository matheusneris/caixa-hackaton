package com.hackaton.simulacaocredito.models.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "TELEMETRIA") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Telemetria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "telemetria_seq")
    @SequenceGenerator(name = "telemetria_seq", sequenceName = "telemetria_seq", allocationSize = 1)
    private Long id;

    @Column(name = "NOME_API", nullable = false)
    private String nomeApi; 

    @Column(name = "TEMPO_RESPOSTA_MS", nullable = false)
    private Long tempoResposta; 

    @Column(name = "STATUS", nullable = false)
    private int status; 

    @Column(name = "DATA_REFERENCIA", nullable = false)
    private LocalDate dataReferencia; 
}
