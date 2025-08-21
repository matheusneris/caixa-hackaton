package com.hackaton.simulacaocredito.exceptions;

public class SimulacaoSemProdutoCompativelException extends RuntimeException {
    public SimulacaoSemProdutoCompativelException() {
        super("Nenhum produto de concessão de crédito disponível para valor e prazo informados");
    }
}
