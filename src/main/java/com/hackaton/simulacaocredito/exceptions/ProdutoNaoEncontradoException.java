package com.hackaton.simulacaocredito.exceptions;

public class ProdutoNaoEncontradoException extends RuntimeException{
    public ProdutoNaoEncontradoException(Long coProduto) {
        super("Produto não encontrado: " + coProduto);
    }
}
