package com.hackaton.simulacaocredito.controllers;

import com.hackaton.simulacaocredito.services.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/produto")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/listar")
    public ResponseEntity buscarProdutos() {
        return ResponseEntity.ok().body(produtoService.listarProdutos());
    }

}
