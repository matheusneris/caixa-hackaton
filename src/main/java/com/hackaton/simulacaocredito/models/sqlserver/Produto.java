package com.hackaton.simulacaocredito.models.sqlserver;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "PRODUTO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Produto {

        @Id
        @Column(name = "CO_PRODUTO")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long coProduto;

        @Column(name = "NO_PRODUTO", nullable = false)
        String noProduto;

        @Column(name = "PC_TAXA_JUROS", precision = 10, scale = 9, nullable = false)
        BigDecimal pcTaxaJuros;

        @Column(name = "NU_MINIMO_MESES", nullable = false)
        Integer nuMinimoMeses;

        @Column(name = "NU_MAXIMO_MESES")
        Integer nuMaximoMeses;

        @Column(name = "VR_MINIMO", nullable = false)
        BigDecimal vrMinimo;

        @Column(name = "VR_MAXIMO")
        BigDecimal vrMaximo;
}
