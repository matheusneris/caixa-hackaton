package com.hackaton.simulacaocredito.models;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idSimulacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_PRODUTO")
    private Produto produto;

    @Column(name = "VALOR_DESEJADO", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorDesejado;

    @Column(name = "PRAZO", nullable = false)
    private Integer prazo;

    @OneToMany(mappedBy = "simulacaoCredito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoSimulacao> resultados = new ArrayList<>();

    @Column(name = "DATA_SIMULACAO", nullable = false)
    private LocalDate dataSimulacao;
}

