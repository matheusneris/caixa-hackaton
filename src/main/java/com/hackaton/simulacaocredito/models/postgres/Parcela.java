package com.hackaton.simulacaocredito.models.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "PARCELA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Parcela {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESULTADO_ID")
    private ResultadoSimulacao resultadoSimulacao;

    @Column(name = "NUMERO", nullable = false)
    private Integer numero;

    @Column(name = "VALOR_AMORTIZACAO", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorAmortizacao;

    @Column(name = "VALOR_JUROS", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorJuros;

    @Column(name = "VALOR_PRESTACAO", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorPrestacao;

    @Column(name = "SALDO_DEVEDOR", nullable = false)
    private BigDecimal saldoDevedor;

}