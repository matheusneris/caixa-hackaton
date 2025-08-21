package com.hackaton.simulacaocredito.services;

import com.hackaton.simulacaocredito.dtos.requests.SimulacoesProdutosRequestDto;
import com.hackaton.simulacaocredito.dtos.responses.*;
import com.hackaton.simulacaocredito.dtos.requests.SimulacaoRequestDto;
import com.hackaton.simulacaocredito.enums.TipoSimulacaoEnum;
import com.hackaton.simulacaocredito.exceptions.SimulacaoSemProdutoCompativelException;
import com.hackaton.simulacaocredito.models.Parcela;
import com.hackaton.simulacaocredito.models.Produto;
import com.hackaton.simulacaocredito.models.ResultadoSimulacao;
import com.hackaton.simulacaocredito.models.SimulacaoCredito;
import com.hackaton.simulacaocredito.repositories.SimulacaoCreditoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SimulacaoCreditoService {

    private final SimulacaoCreditoRepository simulacaoRepository;
    private final ProdutoService produtoService;
    private final TelemetriaService telemetriaService;

    public SimulacaoCreditoService(SimulacaoCreditoRepository simulacaoRepository,
                                   ProdutoService produtoService, TelemetriaService telemetriaService) {
        this.simulacaoRepository = simulacaoRepository;
        this.produtoService = produtoService;
        this.telemetriaService = telemetriaService;
    }

    @Transactional
    public SimulacaoResponseDto fazerSimulacaoCredito(SimulacaoRequestDto request) {
        long start = System.currentTimeMillis();
        try {

            Produto produto = encontrarProdutoParaSimulacao(request);

            SimulacaoCredito simulacaoCredito = new SimulacaoCredito();
            simulacaoCredito.setProduto(produto);
            simulacaoCredito.setValorDesejado(request.valorDesejado());
            simulacaoCredito.setPrazo(request.prazo());
            simulacaoCredito.setDataSimulacao(LocalDate.now());

            ResultadoSimulacao sac = calcularSac(simulacaoCredito);
            ResultadoSimulacao price = calcularPrice(simulacaoCredito);

            simulacaoCredito.getResultados().add(sac);
            simulacaoCredito.getResultados().add(price);

            BigDecimal totalSac = sac.getParcelas().stream()
                    .map(Parcela::getValorPrestacao)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            sac.setValorTotalParcelas(totalSac);
            sac.setValorMedioPrestacao(totalSac.divide(
                    BigDecimal.valueOf(sac.getParcelas().size()), 2, RoundingMode.HALF_UP));

            BigDecimal totalPrice = price.getParcelas().stream()
                    .map(Parcela::getValorPrestacao)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            price.setValorTotalParcelas(totalPrice);
            price.setValorMedioPrestacao(totalPrice.divide(
                    BigDecimal.valueOf(price.getParcelas().size()), 2, RoundingMode.HALF_UP));

            simulacaoCredito.getResultados().add(sac);
            simulacaoCredito.getResultados().add(price);

            simulacaoRepository.save(simulacaoCredito);

            long tempo = System.currentTimeMillis() - start;
            telemetriaService.registrar("Simulação", tempo, 200);

            return new SimulacaoResponseDto(
                    simulacaoCredito.getIdSimulacao(),
                    produto.getCoProduto(),
                    produto.getNoProduto(),
                    produto.getPcTaxaJuros(),
                    List.of(
                            converterParaDto(sac),
                            converterParaDto(price)
                    )
            );
        } catch (SimulacaoSemProdutoCompativelException ex){
            long tempo = System.currentTimeMillis() - start;
            telemetriaService.registrar("Simulação", tempo, 500);
            throw ex;
        } catch (Exception e){
            long tempo = System.currentTimeMillis() - start;
            telemetriaService.registrar("Simulação", tempo, 500);
            return null;
        }
    }

    private Produto encontrarProdutoParaSimulacao(SimulacaoRequestDto request) {
        return produtoService.listarProdutos().stream()
                .filter(p -> {
                    boolean prazoValido = request.prazo() >= p.getNuMinimoMeses()
                            && (p.getNuMaximoMeses() == null || request.prazo() <= p.getNuMaximoMeses());

                    boolean valorValido = request.valorDesejado().compareTo(p.getVrMinimo()) >= 0
                            && (p.getVrMaximo() == null || request.valorDesejado().compareTo(p.getVrMaximo()) <= 0);

                    return prazoValido && valorValido;
                })
                .findFirst()
                .orElseThrow(SimulacaoSemProdutoCompativelException::new);
    }

    private ResultadoSimulacao calcularSac(SimulacaoCredito simulacao) {
        BigDecimal saldoDevedor = simulacao.getValorDesejado();
        BigDecimal taxaMensal = simulacao.getProduto().getPcTaxaJuros();
        int prazo = simulacao.getPrazo();

        BigDecimal amortizacaoConstante = saldoDevedor.divide(BigDecimal.valueOf(prazo), 2, RoundingMode.HALF_UP);

        ResultadoSimulacao resultado = new ResultadoSimulacao();
        resultado.setSimulacaoCredito(simulacao);
        resultado.setTipoSimulacao(TipoSimulacaoEnum.SAC);

        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxaMensal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal prestacao = amortizacaoConstante.add(juros).setScale(2, RoundingMode.HALF_UP);
            BigDecimal saldo = saldoDevedor.subtract(amortizacaoConstante);

            Parcela parcela = new Parcela();
            parcela.setNumero(i);
            parcela.setValorAmortizacao(amortizacaoConstante);
            parcela.setValorJuros(juros);
            parcela.setValorPrestacao(prestacao);
            parcela.setSaldoDevedor(saldo);
            parcela.setResultadoSimulacao(resultado);

            resultado.getParcelas().add(parcela);
            saldoDevedor = saldo;
        }

        return resultado;
    }

    private ResultadoSimulacao calcularPrice(SimulacaoCredito simulacao) {
        BigDecimal saldoDevedor = simulacao.getValorDesejado();
        BigDecimal taxaMensal = simulacao.getProduto().getPcTaxaJuros();
        int prazo = simulacao.getPrazo();

        BigDecimal parcelaConstante = saldoDevedor.multiply(taxaMensal)
                .divide(BigDecimal.ONE.subtract(
                        BigDecimal.ONE.add(taxaMensal).pow(-prazo, MathContext.DECIMAL64)), 2, RoundingMode.HALF_UP);

        ResultadoSimulacao resultado = new ResultadoSimulacao();
        resultado.setSimulacaoCredito(simulacao);
        resultado.setTipoSimulacao(TipoSimulacaoEnum.PRICE);

        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxaMensal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal amortizacao = parcelaConstante.subtract(juros).setScale(2, RoundingMode.HALF_UP);
            BigDecimal saldo = saldoDevedor.subtract(amortizacao);

            Parcela parcela = new Parcela();
            parcela.setNumero(i);
            parcela.setValorAmortizacao(amortizacao);
            parcela.setValorJuros(juros);
            parcela.setValorPrestacao(parcelaConstante);
            parcela.setSaldoDevedor(saldo);
            parcela.setResultadoSimulacao(resultado);

            resultado.getParcelas().add(parcela);
            saldoDevedor = saldo;
        }

        return resultado;
    }

    @Transactional
    public SimulacaoListarResponseDto listarTodasSimulacoes(int pagina, int qtdRegistrosPagina) {
        long start = System.currentTimeMillis();
        try {
            Pageable pageable = PageRequest.of(pagina - 1, qtdRegistrosPagina);
            Page<SimulacaoCredito> page = simulacaoRepository.findAll(pageable);

            List<RegistroListarDto> registros = page.getContent().stream()
                    .map(simulacao -> {
                        BigDecimal menorValorTotalParcelas = simulacao.getResultados().stream()
                                .map(resultado -> resultado.getParcelas().stream()
                                        .map(Parcela::getValorPrestacao)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                                .min(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);

                        return new RegistroListarDto(
                                simulacao.getIdSimulacao(),
                                simulacao.getValorDesejado(),
                                simulacao.getPrazo(),
                                menorValorTotalParcelas
                        );
                    })
                    .toList();

            long tempo = System.currentTimeMillis() - start;
            telemetriaService.registrar("Listar simulações", tempo, 200);

            return new SimulacaoListarResponseDto(
                    pagina,
                    page.getTotalElements(),
                    qtdRegistrosPagina,
                    registros
            );
        } catch (Exception e){
            long tempo = System.currentTimeMillis() - start;
            telemetriaService.registrar("Listar simulações", tempo, 500);
            return null;
        }
    }

    @Transactional
    public SimulacoesProdutosResponseDto listarSimulacoesPorProdutos(SimulacoesProdutosRequestDto request) {
        long start = System.currentTimeMillis();
        try {
            List<SimulacaoCredito> simulacoes = simulacaoRepository.findByProdutoAndDataSimulacao(
                    request.coProduto(),
                    request.dataSimulacao()
            );

            if (simulacoes.isEmpty()) {
                return new SimulacoesProdutosResponseDto(request.dataSimulacao(), List.of());
            }

            Map<Produto, List<SimulacaoCredito>> agrupadoPorProduto =
                    simulacoes.stream().collect(Collectors.groupingBy(SimulacaoCredito::getProduto));

            List<SimulacaoProdutoItemDto> itens = agrupadoPorProduto.entrySet().stream().map(entry -> {
                Produto produto = entry.getKey();
                List<SimulacaoCredito> simsProduto = entry.getValue();

                BigDecimal somaDesejado = simsProduto.stream()
                        .map(SimulacaoCredito::getValorDesejado)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Para cada simulação, pega o valorTotalParcelas do menor entre SAC e PRICE
                BigDecimal somaCredito = simsProduto.stream()
                        .map(sim -> sim.getResultados().stream()
                                .map(ResultadoSimulacao::getValorTotalParcelas)
                                .min(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal taxaMedia = BigDecimal.ZERO;
                if (somaDesejado.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal somaPonderada = simsProduto.stream()
                            .map(sim -> sim.getProduto().getPcTaxaJuros().multiply(sim.getValorDesejado()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    taxaMedia = somaPonderada.divide(somaDesejado, 6, RoundingMode.HALF_UP);
                }

                // Valor médio das prestações (menor entre SAC e PRICE de cada simulação)
                BigDecimal valorMedioPrestacao = simsProduto.stream()
                        .map(sim -> sim.getResultados().stream()
                                .map(ResultadoSimulacao::getValorMedioPrestacao)
                                .min(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(simsProduto.size()), RoundingMode.HALF_UP);

                return new SimulacaoProdutoItemDto(
                        produto.getCoProduto(),
                        produto.getNoProduto(),
                        taxaMedia,
                        valorMedioPrestacao,
                        somaDesejado,
                        somaCredito
                );
            }).toList();

            long tempo = System.currentTimeMillis() - start;
            telemetriaService.registrar("Simulações por produto", tempo, 200);

            return new SimulacoesProdutosResponseDto(request.dataSimulacao(), itens);
        } catch (Exception e){
            long tempo = System.currentTimeMillis() - start;
            telemetriaService.registrar("Simulações por produto", tempo, 500);
            return null;
        }
    }

    private ResultadoSimulacaoDto converterParaDto(ResultadoSimulacao resultado) {
        List<ParcelaDto> parcelasDto = resultado.getParcelas().stream()
                .map(p -> new ParcelaDto(
                        p.getNumero(),
                        p.getValorAmortizacao(),
                        p.getValorJuros(),
                        p.getValorPrestacao()
                ))
                .toList();

        return new ResultadoSimulacaoDto(
                resultado.getTipoSimulacao().name(),
                parcelasDto
        );
    }
}