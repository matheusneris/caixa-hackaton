package com.hackaton.simulacaocredito.exceptions;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(SimulacaoSemProdutoCompativelException.class)
    public String simulacaoSemProdutoCompativel(SimulacaoSemProdutoCompativelException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(RuntimeException.class)
    public String runtimeException(RuntimeException ex){
        return ex.getMessage();
    }
}
