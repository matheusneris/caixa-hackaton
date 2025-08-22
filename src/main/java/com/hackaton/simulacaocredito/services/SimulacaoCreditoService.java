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
    private final EventHubProducerService eventHubService;

    public SimulacaoCreditoService(SimulacaoCreditoRepository simulacaoRepository, ProdutoService produtoService, TelemetriaService telemetriaService, EventHubProducerService eventHubService) {
        this.simulacaoRepository = simulacaoRepository;
        this.produtoService = produtoService;
        this.telemetriaService = telemetriaService;
        this.eventHubService = eventHubService;
    }

    @Transactional
    public SimulacaoResponseDto fazerSimulacaoCredito(SimulacaoRequestDto request) {
        long start = System.currentTimeMillis();
        try {
            Produto produto = consultarProdutoParaSimulacao(request);
            SimulacaoCredito simulacaoCredito = criarSimulacaoCredito(request, produto);
            ResultadoSimulacao sac = calcularSac(simulacaoCredito);
            ResultadoSimulacao price = calcularPrice(simulacaoCredito);

            calcularTotParcelasEMediaPrestacaoSac(sac);
            calcularTotParcelasEMediaPrestacaoPrice(price);

            simulacaoCredito.getResultados().addAll(List.of(sac, price));

            simulacaoRepository.save(simulacaoCredito);

            SimulacaoResponseDto responseDto = construirResponseNovaSimulacao(simulacaoCredito, produto, sac, price);

            eventHubService.enviarSimulacao(responseDto);

            registrarTelemetria(start, "Simulação", 200);

            return responseDto;

        } catch (SimulacaoSemProdutoCompativelException ex){
            registrarTelemetria(start, "Simulação", 500);
            throw ex;
        } catch (Exception e){
            registrarTelemetria(start, "Simulação", 500);
            throw new RuntimeException("Erro não esperado na simulação. Por favor, tente novamente.");
        }
    }

    @Transactional
    public SimulacaoListarResponseDto listarTodasSimulacoes(int pagina, int qtdRegistrosPagina) {
        long start = System.currentTimeMillis();
        try {
            Pageable pageable = PageRequest.of(pagina - 1, qtdRegistrosPagina);
            Page<SimulacaoCredito> page = simulacaoRepository.findAll(pageable);

            List<RegistroListarDto> registros = page.getContent().stream()
                    .map(this::converterSimulacaoParaRegistroListarDto)
                    .toList();

            registrarTelemetria(start, "Listar simulações", 200);

            return construirResponseListarTodas(pagina, qtdRegistrosPagina, page, registros);

        } catch (Exception e){
            registrarTelemetria(start, "Listar simulações", 500);
            throw new RuntimeException("Erro não esperado. Por favor, tente novamente.");
        }
    }

    @Transactional
    public SimulacoesProdutosResponseDto listarSimulacoesPorProdutos(SimulacoesProdutosRequestDto request) {
        long start = System.currentTimeMillis();
        try {
            List<SimulacaoCredito> simulacoes = simulacaoRepository.findByProdutoAndDataSimulacao(request.coProduto(), request.dataSimulacao());

            if (simulacoes.isEmpty()) {
                return new SimulacoesProdutosResponseDto(request.dataSimulacao(), List.of());
            }

            List<SimulacaoProdutoItemDto> itens = converterSimulacoesParaItens(simulacoes);

            registrarTelemetria(start, "Simulações por produto", 200);

            return new SimulacoesProdutosResponseDto(request.dataSimulacao(), itens);

        } catch (Exception e) {
            registrarTelemetria(start, "Simulações por produto", 500);
            throw new RuntimeException("Erro não esperado. Por favor, tente novamente.");
        }
    }

    private static void calcularTotParcelasEMediaPrestacaoSac(ResultadoSimulacao sac) {
        BigDecimal totalSac = sac.getParcelas().stream()
                .map(Parcela::getValorPrestacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sac.setValorTotalParcelas(totalSac);
        sac.setValorMedioPrestacao(totalSac.divide(
                BigDecimal.valueOf(sac.getParcelas().size()), 2, RoundingMode.HALF_UP));
    }

    private static void calcularTotParcelasEMediaPrestacaoPrice(ResultadoSimulacao price) {
        BigDecimal totalPrice = price.getParcelas().stream()
                .map(Parcela::getValorPrestacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        price.setValorTotalParcelas(totalPrice);
        price.setValorMedioPrestacao(totalPrice.divide(
                BigDecimal.valueOf(price.getParcelas().size()), 2, RoundingMode.HALF_UP));
    }

    private SimulacaoCredito criarSimulacaoCredito(SimulacaoRequestDto request, Produto produto) {
        SimulacaoCredito simulacao = new SimulacaoCredito();
        simulacao.setProduto(produto);
        simulacao.setValorDesejado(request.valorDesejado());
        simulacao.setPrazo(request.prazo());
        simulacao.setDataSimulacao(LocalDate.now());
        return simulacao;
    }

    private SimulacaoResponseDto construirResponseNovaSimulacao(SimulacaoCredito simulacaoCredito, Produto produto, ResultadoSimulacao sac, ResultadoSimulacao price) {
        return new SimulacaoResponseDto(
                simulacaoCredito.getIdSimulacao(),
                produto.getCoProduto(),
                produto.getNoProduto(),
                produto.getPcTaxaJuros(),
                List.of(converterSimulacaoParaDto(sac), converterSimulacaoParaDto(price)));
    }

    private Produto consultarProdutoParaSimulacao(SimulacaoRequestDto request) {
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
        BigDecimal amortizacaoConstante = saldoDevedor.divide(BigDecimal.valueOf(prazo), 10, RoundingMode.HALF_UP);
        //BigDecimal amortizacaoConstante = saldoDevedor.divide(BigDecimal.valueOf(prazo), 2, RoundingMode.HALF_UP);

        ResultadoSimulacao resultado = new ResultadoSimulacao();
        resultado.setSimulacaoCredito(simulacao);
        resultado.setTipoSimulacao(TipoSimulacaoEnum.SAC);

        calcularComponentesParcelasSac(prazo, saldoDevedor, taxaMensal, amortizacaoConstante, resultado);

        return resultado;
    }

    private static void calcularComponentesParcelasSac(int prazo, BigDecimal saldoDevedor, BigDecimal taxaMensal, BigDecimal amortizacaoConstante, ResultadoSimulacao resultado) {
        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxaMensal);
            BigDecimal prestacao = amortizacaoConstante.add(juros);
            BigDecimal saldo = saldoDevedor.subtract(amortizacaoConstante);

            Parcela parcela = new Parcela();
            parcela.setNumero(i);
            parcela.setValorAmortizacao(amortizacaoConstante.setScale(2, RoundingMode.HALF_UP));
            parcela.setValorJuros(juros.setScale(2, RoundingMode.HALF_UP));
            parcela.setValorPrestacao(prestacao.setScale(2, RoundingMode.HALF_UP));
            parcela.setSaldoDevedor(saldoDevedor.subtract(amortizacaoConstante).setScale(2, RoundingMode.HALF_UP));
            parcela.setResultadoSimulacao(resultado);

            resultado.getParcelas().add(parcela);
            saldoDevedor = saldo;
        }
    }

    private ResultadoSimulacao calcularPrice(SimulacaoCredito simulacao) {
        BigDecimal saldoDevedor = simulacao.getValorDesejado();
        BigDecimal taxaMensal = simulacao.getProduto().getPcTaxaJuros();
        int prazo = simulacao.getPrazo();

        BigDecimal parcelaConstante = saldoDevedor.multiply(taxaMensal)
                .divide(BigDecimal.ONE.subtract(
                                BigDecimal.ONE.add(taxaMensal).pow(-prazo, MathContext.DECIMAL128)),
                        10, RoundingMode.HALF_UP);

        ResultadoSimulacao resultado = new ResultadoSimulacao();
        resultado.setSimulacaoCredito(simulacao);
        resultado.setTipoSimulacao(TipoSimulacaoEnum.PRICE);

        calcularComponentesParcelaPrice(prazo, saldoDevedor, taxaMensal, parcelaConstante, resultado);

        return resultado;
    }

    private static void calcularComponentesParcelaPrice(int prazo, BigDecimal saldoDevedor, BigDecimal taxaMensal,
                                                        BigDecimal parcelaConstante, ResultadoSimulacao resultado) {
        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxaMensal);
            BigDecimal amortizacao = parcelaConstante.subtract(juros);
            BigDecimal saldo = saldoDevedor.subtract(amortizacao);

            Parcela parcela = new Parcela();
            parcela.setNumero(i);
            parcela.setValorAmortizacao(amortizacao.setScale(2, RoundingMode.HALF_UP));
            parcela.setValorJuros(juros.setScale(2, RoundingMode.HALF_UP));
            parcela.setValorPrestacao(parcelaConstante.setScale(2, RoundingMode.HALF_UP));
            parcela.setSaldoDevedor(saldo.setScale(2, RoundingMode.HALF_UP));
            parcela.setResultadoSimulacao(resultado);

            resultado.getParcelas().add(parcela);
            saldoDevedor = saldo;
        }
    }

    private RegistroListarDto converterSimulacaoParaRegistroListarDto(SimulacaoCredito simulacao) {
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
    }

    private static SimulacaoListarResponseDto construirResponseListarTodas(int pagina, int qtdRegistrosPagina, Page<SimulacaoCredito> page, List<RegistroListarDto> registros) {
        return new SimulacaoListarResponseDto(
                pagina,
                page.getTotalElements(),
                qtdRegistrosPagina,
                registros
        );
    }

    private List<SimulacaoProdutoItemDto> converterSimulacoesParaItens(List<SimulacaoCredito> simulacoes) {
        Map<Produto, List<SimulacaoCredito>> agrupadoPorProduto =
                simulacoes.stream().collect(Collectors.groupingBy(SimulacaoCredito::getProduto));

        return agrupadoPorProduto.entrySet().stream()
                .map(entry -> criarItemProduto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private SimulacaoProdutoItemDto criarItemProduto(Produto produto, List<SimulacaoCredito> simsProduto) {
        BigDecimal somaDesejado = calcularSomaDesejado(simsProduto);
        BigDecimal somaCredito = calcularSomaCredito(simsProduto);
        BigDecimal taxaMedia = calcularTaxaMedia(simsProduto, somaDesejado);
        BigDecimal valorMedioPrestacao = calcularValorMedioPrestacao(simsProduto);

        return new SimulacaoProdutoItemDto(
                produto.getCoProduto(),
                produto.getNoProduto(),
                taxaMedia,
                valorMedioPrestacao,
                somaDesejado,
                somaCredito
        );
    }

    private BigDecimal calcularSomaDesejado(List<SimulacaoCredito> simsProduto) {
        return simsProduto.stream()
                .map(SimulacaoCredito::getValorDesejado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularSomaCredito(List<SimulacaoCredito> simsProduto) {
        return simsProduto.stream()
                .map(sim -> sim.getResultados().stream()
                        .map(ResultadoSimulacao::getValorTotalParcelas)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTaxaMedia(List<SimulacaoCredito> simsProduto, BigDecimal somaDesejado) {
        if (somaDesejado.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal somaPonderada = simsProduto.stream()
                .map(sim -> sim.getProduto().getPcTaxaJuros().multiply(sim.getValorDesejado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return somaPonderada.divide(somaDesejado, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularValorMedioPrestacao(List<SimulacaoCredito> simsProduto) {
        return simsProduto.stream()
                .map(sim -> sim.getResultados().stream()
                        .map(ResultadoSimulacao::getValorMedioPrestacao)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(simsProduto.size()), RoundingMode.HALF_UP);
    }

    private ResultadoSimulacaoDto converterSimulacaoParaDto(ResultadoSimulacao resultado) {
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

    private void registrarTelemetria(long start, String nomeEndPoint, int status) {
        long tempo = System.currentTimeMillis() - start;
        telemetriaService.registrar(nomeEndPoint, tempo, status);
    }
}