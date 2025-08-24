package com.hackaton.simulacaocredito.models.postgres;

import com.hackaton.simulacaocredito.enums.TipoSimulacaoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RESULTADO_SIMULACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoSimulacao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resultado_simulacao_seq")
    @SequenceGenerator(name = "resultado_simulacao_seq", sequenceName = "resultado_simulacao_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SIMULACAO_ID")
    private SimulacaoCredito simulacaoCredito;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false)
    private TipoSimulacaoEnum tipoSimulacao;

    @OneToMany(mappedBy = "resultadoSimulacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Parcela> parcelas = new ArrayList<>();

    @Column(name = "VALOR_TOTAL_PARCELAS", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotalParcelas;

    @Column(name = "VALOR_MEDIO_PRESTACAO", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorMedioPrestacao;
}
