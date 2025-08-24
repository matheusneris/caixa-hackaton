package com.hackaton.simulacaocredito.models.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SIMULACAO_CREDITO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "simulacao_credito_seq")
    @SequenceGenerator(name = "simulacao_credito_seq", sequenceName = "simulacao_credito_seq", allocationSize = 1)
    private Long idSimulacao;

    @Column(name = "CO_PRODUTO", nullable = false)
    private Long coProduto;

    @Column(name = "VALOR_DESEJADO", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorDesejado;

    @Column(name = "PRAZO", nullable = false)
    private Integer prazo;

    @OneToMany(mappedBy = "simulacaoCredito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoSimulacao> resultados = new ArrayList<>();

    @Column(name = "DATA_SIMULACAO", nullable = false)
    private LocalDate dataSimulacao;
}
