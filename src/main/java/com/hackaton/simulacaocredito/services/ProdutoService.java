package com.hackaton.simulacaocredito.services;

import com.hackaton.simulacaocredito.dtos.requests.SimulacaoRequestDto;
import com.hackaton.simulacaocredito.exceptions.SimulacaoSemProdutoCompativelException;
import com.hackaton.simulacaocredito.models.sqlserver.Produto;
import com.hackaton.simulacaocredito.repositories.sqlserver.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<Produto> listarProdutos() {
        return repository.findAll();
    }

    public Produto consultarProdutoParaSimulacao(SimulacaoRequestDto request) {
        return listarProdutos().stream()
                .filter(p -> request.prazo() >= p.getNuMinimoMeses()
                        && (p.getNuMaximoMeses() == null || request.prazo() <= p.getNuMaximoMeses())
                        && request.valorDesejado().compareTo(p.getVrMinimo()) >= 0
                        && (p.getVrMaximo() == null || request.valorDesejado().compareTo(p.getVrMaximo()) <= 0))
                .findFirst()
                .orElseThrow(SimulacaoSemProdutoCompativelException::new);
    }
}
