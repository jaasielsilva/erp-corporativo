package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.FluxoCaixa;
import com.jaasielsilva.portalceo.model.ContaPagar;
import com.jaasielsilva.portalceo.model.ContaReceber;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.FluxoCaixaRepository;
import com.jaasielsilva.portalceo.repository.ContaPagarRepository;
import com.jaasielsilva.portalceo.repository.ContaReceberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FluxoCaixaService {

    private final FluxoCaixaRepository fluxoCaixaRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final ContaReceberRepository contaReceberRepository;

    // CRUD Operations
    public FluxoCaixa save(FluxoCaixa fluxoCaixa) {
        validarFluxoCaixa(fluxoCaixa);
        
        if (fluxoCaixa.getId() == null) {
            fluxoCaixa.setDataCriacao(LocalDateTime.now());
            if (fluxoCaixa.getStatus() == null) {
                fluxoCaixa.setStatus(FluxoCaixa.StatusFluxo.PREVISTO);
            }
        }
        
        return fluxoCaixaRepository.save(fluxoCaixa);
    }

    @Transactional(readOnly = true)
    public Optional<FluxoCaixa> findById(Long id) {
        return fluxoCaixaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<FluxoCaixa> findAll() {
        return fluxoCaixaRepository.findAll();
    }

    public void deleteById(Long id) {
        fluxoCaixaRepository.findById(id).ifPresent(fluxo -> {
            if (fluxo.getStatus() == FluxoCaixa.StatusFluxo.REALIZADO) {
                throw new IllegalStateException("Não é possível excluir uma transação já realizada");
            }
            fluxoCaixaRepository.deleteById(id);
        });
    }

    // Business Operations
    @Transactional
    public FluxoCaixa realizarTransacao(Long id, String observacoes, Usuario usuario) {
        FluxoCaixa fluxo = fluxoCaixaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada"));

        if (fluxo.getStatus() == FluxoCaixa.StatusFluxo.REALIZADO) {
            throw new IllegalStateException("Transação já foi realizada");
        }

        fluxo.setStatus(FluxoCaixa.StatusFluxo.REALIZADO);
        fluxo.setObservacoes(observacoes);
        fluxo.setDataUltimaEdicao(LocalDateTime.now());

        return fluxoCaixaRepository.save(fluxo);
    }

    @Transactional
    public FluxoCaixa cancelarTransacao(Long id, String motivoCancelamento, Usuario usuario) {
        FluxoCaixa fluxo = fluxoCaixaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada"));

        if (fluxo.getStatus() == FluxoCaixa.StatusFluxo.REALIZADO) {
            throw new IllegalStateException("Não é possível cancelar uma transação já realizada");
        }

        fluxo.setStatus(FluxoCaixa.StatusFluxo.CANCELADO);
        fluxo.setObservacoes(motivoCancelamento);
        fluxo.setDataUltimaEdicao(LocalDateTime.now());

        return fluxoCaixaRepository.save(fluxo);
    }

    @Transactional
    public void sincronizarContasPagar() {
        List<ContaPagar> contasPendentes = contaPagarRepository.findByStatusOrderByDataVencimento(ContaPagar.StatusContaPagar.APROVADA);
        
        for (ContaPagar conta : contasPendentes) {
            // Check if cash flow entry already exists
            List<FluxoCaixa> existingEntries = fluxoCaixaRepository.findByContaPagar(conta.getId());
            
            if (existingEntries.isEmpty()) {
                FluxoCaixa fluxo = new FluxoCaixa();
                fluxo.setData(conta.getDataVencimento());
                fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.SAIDA);
                fluxo.setCategoria(mapearCategoriaContaPagar(conta.getCategoria()));
                fluxo.setDescricao("Previsão - " + conta.getDescricao());
                fluxo.setValor(conta.getValorTotal());
                fluxo.setStatus(FluxoCaixa.StatusFluxo.PREVISTO);
                fluxo.setContaPagar(conta);
                fluxo.setNumeroDocumento(conta.getNumeroDocumento());
                fluxo.setDataCriacao(LocalDateTime.now());
                
                fluxoCaixaRepository.save(fluxo);
            }
        }
    }

    @Transactional
    public void sincronizarContasReceber() {
        List<ContaReceber> contasPendentes = contaReceberRepository.findByStatusOrderByDataVencimento(ContaReceber.StatusContaReceber.PENDENTE);
        
        for (ContaReceber conta : contasPendentes) {
            // Check if cash flow entry already exists
            List<FluxoCaixa> existingEntries = fluxoCaixaRepository.findByContaReceber(conta.getId());
            
            if (existingEntries.isEmpty()) {
                FluxoCaixa fluxo = new FluxoCaixa();
                fluxo.setData(conta.getDataVencimento());
                fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.ENTRADA);
                fluxo.setCategoria(FluxoCaixa.CategoriaFluxo.VENDAS);
                fluxo.setDescricao("Previsão - " + conta.getDescricao());
                fluxo.setValor(conta.getValorTotal());
                fluxo.setStatus(FluxoCaixa.StatusFluxo.PREVISTO);
                fluxo.setContaReceber(conta);
                fluxo.setNumeroDocumento(conta.getNumeroDocumento());
                fluxo.setDataCriacao(LocalDateTime.now());
                
                fluxoCaixaRepository.save(fluxo);
            }
        }
    }

    // Query Operations
    @Transactional(readOnly = true)
    public List<FluxoCaixa> findByPeriodo(LocalDate inicio, LocalDate fim) {
        return fluxoCaixaRepository.findByDataBetweenOrderByData(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<FluxoCaixa> findByTipoMovimento(FluxoCaixa.TipoMovimento tipo, LocalDate inicio, LocalDate fim) {
        return fluxoCaixaRepository.findByTipoMovimentoAndDataBetweenOrderByData(tipo, inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<FluxoCaixa> findByCategoria(FluxoCaixa.CategoriaFluxo categoria, LocalDate inicio, LocalDate fim) {
        return fluxoCaixaRepository.findByCategoriaAndDataBetweenOrderByData(categoria, inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<FluxoCaixa> findByStatus(FluxoCaixa.StatusFluxo status, LocalDate inicio, LocalDate fim) {
        return fluxoCaixaRepository.findByStatusAndDataBetweenOrderByData(status, inicio, fim);
    }

    // Financial Analysis
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoAtual() {
        return fluxoCaixaRepository.calcularSaldoAcumuladoAte(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoPeriodo(LocalDate inicio, LocalDate fim) {
        BigDecimal saldo = fluxoCaixaRepository.calcularSaldoByPeriodo(inicio, fim);
        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularEntradasPeriodo(LocalDate inicio, LocalDate fim) {
        BigDecimal entradas = fluxoCaixaRepository.sumEntradasByPeriodo(inicio, fim);
        return entradas != null ? entradas : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaidasPeriodo(LocalDate inicio, LocalDate fim) {
        BigDecimal saidas = fluxoCaixaRepository.sumSaidasByPeriodo(inicio, fim);
        return saidas != null ? saidas : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getResumoFinanceiroPeriodo(LocalDate inicio, LocalDate fim) {
        BigDecimal entradas = calcularEntradasPeriodo(inicio, fim);
        BigDecimal saidas = calcularSaidasPeriodo(inicio, fim);
        BigDecimal saldo = entradas.subtract(saidas);

        return Map.of(
            "entradas", entradas,
            "saidas", saidas,
            "saldo", saldo
        );
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getProjecaoFinanceira(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate futuro = hoje.plusDays(dias);

        BigDecimal entradasPrevistas = fluxoCaixaRepository.sumEntradasPrevistasByPeriodo(hoje, futuro);
        BigDecimal saidasPrevistas = fluxoCaixaRepository.sumSaidasPrevistasByPeriodo(hoje, futuro);
        BigDecimal saldoAtual = calcularSaldoAtual();
        BigDecimal saldoProjetado = saldoAtual.add(entradasPrevistas != null ? entradasPrevistas : BigDecimal.ZERO)
                                             .subtract(saidasPrevistas != null ? saidasPrevistas : BigDecimal.ZERO);

        return Map.of(
            "saldoAtual", saldoAtual,
            "entradasPrevistas", entradasPrevistas != null ? entradasPrevistas : BigDecimal.ZERO,
            "saidasPrevistas", saidasPrevistas != null ? saidasPrevistas : BigDecimal.ZERO,
            "saldoProjetado", saldoProjetado
        );
    }

    @Transactional(readOnly = true)
    public List<Object[]> getFluxoDiario(LocalDate inicio, LocalDate fim) {
        return fluxoCaixaRepository.getFluxoDiario(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getFluxoMensal(LocalDate inicio, LocalDate fim) {
        return fluxoCaixaRepository.getFluxoMensal(inicio, fim);
    }

    @Transactional(readOnly = true)
    public Map<FluxoCaixa.CategoriaFluxo, BigDecimal> getEntradasPorCategoria(LocalDate inicio, LocalDate fim) {
        List<Object[]> results = fluxoCaixaRepository.sumValorByCategoriaAndTipoMovimento(
            FluxoCaixa.TipoMovimento.ENTRADA, inicio, fim);
        
        return results.stream()
            .collect(Collectors.toMap(
                row -> (FluxoCaixa.CategoriaFluxo) row[0],
                row -> (BigDecimal) row[1]
            ));
    }

    @Transactional(readOnly = true)
    public Map<FluxoCaixa.CategoriaFluxo, BigDecimal> getSaidasPorCategoria(LocalDate inicio, LocalDate fim) {
        List<Object[]> results = fluxoCaixaRepository.sumValorByCategoriaAndTipoMovimento(
            FluxoCaixa.TipoMovimento.SAIDA, inicio, fim);
        
        return results.stream()
            .collect(Collectors.toMap(
                row -> (FluxoCaixa.CategoriaFluxo) row[0],
                row -> (BigDecimal) row[1]
            ));
    }

    // Dashboard Statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = YearMonth.from(hoje).atDay(1);
        LocalDate fimMes = YearMonth.from(hoje).atEndOfMonth();

        Map<String, BigDecimal> resumoMes = getResumoFinanceiroPeriodo(inicioMes, fimMes);
        Map<String, BigDecimal> projecao30dias = getProjecaoFinanceira(30);

        return Map.of(
            "saldoAtual", calcularSaldoAtual(),
            "entradasMes", resumoMes.get("entradas"),
            "saidasMes", resumoMes.get("saidas"),
            "saldoMes", resumoMes.get("saldo"),
            "saldoProjetado30dias", projecao30dias.get("saldoProjetado"),
            "totalTransacoesMes", fluxoCaixaRepository.countByStatusAndPeriodo(FluxoCaixa.StatusFluxo.REALIZADO, inicioMes, fimMes)
        );
    }

    // Private helper methods
    private void validarFluxoCaixa(FluxoCaixa fluxo) {
        if (fluxo.getValor() == null || fluxo.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        
        if (fluxo.getData() == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        
        if (fluxo.getTipoMovimento() == null) {
            throw new IllegalArgumentException("Tipo de movimento é obrigatório");
        }
        
        if (fluxo.getCategoria() == null) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        
        if (fluxo.getNumeroDocumento() != null && 
            fluxoCaixaRepository.existsByNumeroDocumento(fluxo.getNumeroDocumento())) {
            throw new IllegalArgumentException("Número do documento já existe");
        }
    }

    private FluxoCaixa.CategoriaFluxo mapearCategoriaContaPagar(ContaPagar.CategoriaContaPagar categoria) {
        switch (categoria) {
            case FORNECEDORES:
                return FluxoCaixa.CategoriaFluxo.FORNECEDORES;
            case SALARIOS:
                return FluxoCaixa.CategoriaFluxo.SALARIOS;
            case IMPOSTOS:
                return FluxoCaixa.CategoriaFluxo.IMPOSTOS;
            case ALUGUEL:
                return FluxoCaixa.CategoriaFluxo.ALUGUEL;
            case CONSULTORIA:
                return FluxoCaixa.CategoriaFluxo.SERVICOS;
            case ENERGIA:
            case COMBUSTIVEL:
            case MANUTENCAO:
            case TELEFONE:
                return FluxoCaixa.CategoriaFluxo.OUTRAS_DESPESAS;
            case MARKETING:
                return FluxoCaixa.CategoriaFluxo.MARKETING;
            case OUTROS:
            default:
                return FluxoCaixa.CategoriaFluxo.OUTRAS_DESPESAS;
        }
    }
}