package com.hackaton.simulacaocredito.repositories;

import com.hackaton.simulacaocredito.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
