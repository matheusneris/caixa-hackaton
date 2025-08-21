package com.hackaton.simulacaocredito.services;

import com.hackaton.simulacaocredito.models.Produto;
import com.hackaton.simulacaocredito.repositories.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public List<Produto> listarProdutos() {
        return repository.findAll();
    }

    @Transactional
    public void salvarProduto(Produto produto){
        repository.save(produto);
    }
}
