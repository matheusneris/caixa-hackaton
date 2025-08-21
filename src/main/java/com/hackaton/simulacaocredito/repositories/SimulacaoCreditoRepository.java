package com.hackaton.simulacaocredito.repositories;

import com.hackaton.simulacaocredito.models.SimulacaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SimulacaoCreditoRepository extends JpaRepository<SimulacaoCredito, Long> {

    @Query("SELECT s FROM SimulacaoCredito s WHERE s.produto.coProduto = :coProduto AND s.dataSimulacao = :data")
    List<SimulacaoCredito> findByProdutoAndDataSimulacao(@Param("coProduto") Long coProduto,
                                                         @Param("data") LocalDate data);


}
