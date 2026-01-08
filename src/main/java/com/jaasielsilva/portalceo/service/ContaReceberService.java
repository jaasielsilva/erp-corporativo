package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.ContaReceber;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.FluxoCaixa;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.HistoricoContaReceber;
import com.jaasielsilva.portalceo.repository.ContaReceberRepository;
import com.jaasielsilva.portalceo.repository.FluxoCaixaRepository;
import com.jaasielsilva.portalceo.repository.HistoricoContaReceberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContaReceberService {

    private final ContaReceberRepository contaReceberRepository;
    private final FluxoCaixaRepository fluxoCaixaRepository;
    private final HistoricoContaReceberRepository historicoReceberRepository;
    private final ContabilidadeService contabilidadeService;

    // ---------------- CRUD ----------------
    public ContaReceber save(ContaReceber contaReceber) {
        validarContaReceber(contaReceber);

        boolean criando = contaReceber.getId() == null;
        if (criando) {
            contaReceber.setDataCriacao(LocalDateTime.now());
            if (contaReceber.getStatus() == null) {
                contaReceber.setStatus(ContaReceber.StatusContaReceber.PENDENTE);
            }
        }

        ContaReceber savedConta = contaReceberRepository.save(contaReceber);
        criarEntradaFluxoCaixa(savedConta);
        if (criando) {
            contabilidadeService.registrarReceitaCompetencia(savedConta);
        }

        return savedConta;
    }

    // NOVO: Salvar com usuário logado para auditoria
    public ContaReceber save(ContaReceber contaReceber, Usuario usuario) {
        validarContaReceber(contaReceber);

        boolean criando = contaReceber.getId() == null;
        if (criando) {
            contaReceber.setDataCriacao(LocalDateTime.now());
            if (usuario != null) {
                contaReceber.setUsuarioCriacao(usuario);
            }
            if (contaReceber.getStatus() == null) {
                contaReceber.setStatus(ContaReceber.StatusContaReceber.PENDENTE);
            }
        }

        ContaReceber savedConta = contaReceberRepository.save(contaReceber);
        criarEntradaFluxoCaixa(savedConta, usuario);
        if (criando) {
            contabilidadeService.registrarReceitaCompetencia(savedConta);
            registrarHistorico(savedConta, usuario, "CONTA_CRIADA", null);
        }
        return savedConta;
    }

    @Transactional(readOnly = true)
    public Optional<ContaReceber> findById(Long id) {
        return contaReceberRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> findAll() {
        return contaReceberRepository.findAllWithCliente();
    }

    public void deleteById(Long id) {
        contaReceberRepository.findById(id).ifPresent(conta -> {
            if (conta.getStatus() == ContaReceber.StatusContaReceber.RECEBIDA) {
                throw new IllegalStateException("Não é possível excluir uma conta já recebida");
            }
            contaReceberRepository.deleteById(id);
        });
    }

    // Excluir com auditoria: cancela fluxos previstos e registra usuário
    public void deleteById(Long id, Usuario usuario) {
        contaReceberRepository.findById(id).ifPresent(conta -> {
            if (conta.getStatus() == ContaReceber.StatusContaReceber.RECEBIDA) {
                throw new IllegalStateException("Não é possível excluir uma conta já recebida");
            }

            List<FluxoCaixa> fluxos = fluxoCaixaRepository.findByContaReceber(conta.getId());
            for (FluxoCaixa f : fluxos) {
                if (f.getStatus() == FluxoCaixa.StatusFluxo.PREVISTO) {
                    f.setStatus(FluxoCaixa.StatusFluxo.CANCELADO);
                    f.setUsuarioCriacao(usuario);
                    f.setObservacoes("Exclusão da conta a receber");
                    fluxoCaixaRepository.save(f);
                }
            }

            registrarHistorico(conta, usuario, "CONTA_EXCLUIDA", "Exclusão da conta a receber");
            contaReceberRepository.deleteById(id);
        });
    }

    // ---------------- Recebimento ----------------
    @Transactional
    public ContaReceber receberConta(Long id, BigDecimal valorRecebido, String observacoes, Usuario usuario) {
        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada"));

        if (conta.getStatus() == ContaReceber.StatusContaReceber.CANCELADA) {
            throw new IllegalStateException("Não é possível receber uma conta cancelada");
        }

        if (conta.getStatus() == ContaReceber.StatusContaReceber.RECEBIDA) {
            throw new IllegalStateException("Esta conta já foi totalmente recebida.");
        }

        BigDecimal valorTotalDevido = conta.getValorTotal();
        BigDecimal novoValorRecebido = conta.getValorRecebido().add(valorRecebido);

        if (novoValorRecebido.compareTo(valorTotalDevido) > 0) {
            throw new IllegalArgumentException("Valor recebido excede o valor total da conta");
        }

        conta.setValorRecebido(novoValorRecebido);
        conta.setDataRecebimento(LocalDate.now());
        conta.setObservacoes(observacoes);

        if (novoValorRecebido.compareTo(valorTotalDevido) == 0) {
            conta.setStatus(ContaReceber.StatusContaReceber.RECEBIDA);
        } else {
            conta.setStatus(ContaReceber.StatusContaReceber.PARCIAL);
        }

        ContaReceber contaSalva = contaReceberRepository.save(conta);
        FluxoCaixa fluxoRecebimento = criarEntradaFluxoCaixaRecebimento(contaSalva, valorRecebido, usuario);
        contabilidadeService.registrarRecebimento(contaSalva, valorRecebido, null,
                fluxoRecebimento != null ? fluxoRecebimento.getId() : null);
        registrarHistorico(contaSalva, usuario, "RECEBIMENTO_REGISTRADO", observacoes);

        return contaSalva;
    }

    // ---------------- Cancelamento ----------------
    @Transactional
    public ContaReceber cancelarConta(Long id, String motivoCancelamento, Usuario usuario) {
        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada"));

        if (conta.getStatus() == ContaReceber.StatusContaReceber.RECEBIDA) {
            throw new IllegalStateException("Não é possível cancelar uma conta já recebida");
        }

        conta.setStatus(ContaReceber.StatusContaReceber.CANCELADA);
        conta.setObservacoes(motivoCancelamento);

        // Cancelar entradas previstas do fluxo de caixa vinculadas a esta conta
        List<FluxoCaixa> fluxos = fluxoCaixaRepository.findByContaReceber(conta.getId());
        for (FluxoCaixa f : fluxos) {
            if (f.getStatus() == FluxoCaixa.StatusFluxo.PREVISTO) {
                f.setStatus(FluxoCaixa.StatusFluxo.CANCELADO);
                f.setUsuarioCriacao(usuario);
                f.setObservacoes("Cancelamento: " + motivoCancelamento);
                fluxoCaixaRepository.save(f);
            }
        }

        return contaReceberRepository.save(conta);
    }

    // ---------------- Juros e Multas ----------------
    @Transactional
    public void processarJurosMultas() {
        LocalDate hoje = LocalDate.now();
        List<ContaReceber> contasVencidas = contaReceberRepository.findByDataVencimentoBeforeAndStatusIn(
                hoje, List.of(ContaReceber.StatusContaReceber.PENDENTE, ContaReceber.StatusContaReceber.PARCIAL));

        for (ContaReceber conta : contasVencidas) {
            calcularJurosMulta(conta, hoje);

            long diasAtraso = ChronoUnit.DAYS.between(conta.getDataVencimento(), hoje);
            if (diasAtraso > 30 && conta.getStatus() != ContaReceber.StatusContaReceber.INADIMPLENTE) {
                conta.setStatus(ContaReceber.StatusContaReceber.INADIMPLENTE);
            } else if (conta.getStatus() == ContaReceber.StatusContaReceber.PENDENTE) {
                conta.setStatus(ContaReceber.StatusContaReceber.VENCIDA);
            }

            contaReceberRepository.save(conta);
        }
    }

    // ---------------- Consultas ----------------
    @Transactional(readOnly = true)
    public List<ContaReceber> findByStatus(ContaReceber.StatusContaReceber status) {
        return contaReceberRepository.findByStatusWithCliente(status);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> findByCliente(Cliente cliente) {
        return contaReceberRepository.findByClienteOrderByDataVencimento(cliente);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return contaReceberRepository.findByPeriodo(dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> findVencidas() {
        return contaReceberRepository.findVencidas(
                LocalDate.now(),
                List.of(ContaReceber.StatusContaReceber.PENDENTE, ContaReceber.StatusContaReceber.PARCIAL));
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> findInadimplentes() {
        return contaReceberRepository.findInadimplentes();
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> findVencendoEm(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate futuro = hoje.plusDays(dias);
        return contaReceberRepository.findVencendoEm(hoje, futuro);
    }

    @Transactional(readOnly = true)
    public Page<ContaReceber> findAllPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contaReceberRepository.findAll(pageable);
    }

    // ---------------- Relatórios Financeiros ----------------
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalReceber() {
        BigDecimal total = contaReceberRepository.sumSaldoReceber();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalRecebidoPeriodo(LocalDate inicio, LocalDate fim) {
        BigDecimal total = contaReceberRepository.sumValorRecebidoByPeriodo(inicio, fim);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Map<ContaReceber.StatusContaReceber, Long> getEstatisticasPorStatus() {
        return List.of(ContaReceber.StatusContaReceber.values())
                .stream()
                .collect(Collectors.toMap(
                        status -> status,
                        status -> {
                            Long count = contaReceberRepository.countByStatus(status);
                            return count != null ? count : 0L;
                        }));
    }

    @Transactional(readOnly = true)
    public Map<ContaReceber.CategoriaContaReceber, BigDecimal> getRecebimentosPorCategoria(LocalDate inicio,
            LocalDate fim) {
        List<Object[]> results = contaReceberRepository.sumValorRecebidoByCategoriaPeriodo(inicio, fim);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (ContaReceber.CategoriaContaReceber) row[0],
                        row -> (BigDecimal) row[1]));
    }

    @Transactional(readOnly = true)
    public Map<java.time.YearMonth, BigDecimal> getRecebimentosPorMes(LocalDate inicio, LocalDate fim) {
        List<Object[]> rows = contaReceberRepository.sumRecebidoPorMes(inicio, fim);
        Map<java.time.YearMonth, BigDecimal> mapa = new java.util.LinkedHashMap<>();
        for (Object[] r : rows) {
            Integer ano = (Integer) r[0];
            Integer mes = (Integer) r[1];
            BigDecimal total = (BigDecimal) r[2];
            if (ano != null && mes != null) {
                mapa.put(java.time.YearMonth.of(ano, mes), total != null ? total : BigDecimal.ZERO);
            }
        }
        return mapa;
    }

    @Transactional(readOnly = true)
    public long contarRecebimentosPeriodo(LocalDate inicio, LocalDate fim) {
        Long count = contaReceberRepository.countRecebidasPorPeriodo(inicio, fim);
        return count != null ? count : 0L;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getSaldoPorCliente() {
        return contaReceberRepository.sumSaldoReceberByCliente();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getFluxoRecebimentoDiario(LocalDate inicio, LocalDate fim) {
        return contaReceberRepository.sumValorRecebidoPorDia(inicio, fim);
    }

    // ---------------- Aging Analysis ----------------
    @Transactional(readOnly = true)
    public Map<String, Object> getAnaliseIdade() {
        LocalDate hoje = LocalDate.now();
        return Map.of(
                "ate30dias", contaReceberRepository.getAgingData(hoje.minusDays(30), hoje),
                "de31a60dias", contaReceberRepository.getAgingData(hoje.minusDays(60), hoje.minusDays(31)),
                "de61a90dias", contaReceberRepository.getAgingData(hoje.minusDays(90), hoje.minusDays(61)),
                "acima90dias", contaReceberRepository.getAgingData(LocalDate.of(1900, 1, 1), hoje.minusDays(91)));
    }

    // ---------------- Novo: Resumo para o front ----------------
    @Transactional(readOnly = true)
    public Map<String, Long> getResumoContasParaFront() {
        return Map.of(
                "vencidas", contarVencidas(),
                "pendentes", contaReceberRepository.countByStatus(ContaReceber.StatusContaReceber.PENDENTE),
                "parciais", contaReceberRepository.countByStatus(ContaReceber.StatusContaReceber.PARCIAL),
                "inadimplentes", contaReceberRepository.countByStatus(ContaReceber.StatusContaReceber.INADIMPLENTE),
                "recebidas", contaReceberRepository.countByStatus(ContaReceber.StatusContaReceber.RECEBIDA)).entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? e.getValue() : 0L));
    }

    @Transactional(readOnly = true)
    public long contarVencidas() {
        Long count = contaReceberRepository.countVencidas(LocalDate.now());
        return count != null ? count : 0L;
    }

    // ---------------- Helper Methods ----------------
    private void validarContaReceber(ContaReceber conta) {
        if (conta.getValorOriginal() == null || conta.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor original deve ser maior que zero");
        }

        if (conta.getDataVencimento() == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }

        if (conta.getCliente() == null) {
            throw new IllegalArgumentException("Cliente é obrigatório");
        }

        if (conta.getNumeroDocumento() != null &&
                contaReceberRepository.existsByNumeroDocumento(conta.getNumeroDocumento())) {
            throw new IllegalArgumentException("Número do documento já existe");
        }
    }

    private void calcularJurosMulta(ContaReceber conta, LocalDate dataAtual) {
        long diasAtraso = ChronoUnit.DAYS.between(conta.getDataVencimento(), dataAtual);

        if (diasAtraso > 0) {
            BigDecimal multa = conta.getValorOriginal().multiply(BigDecimal.valueOf(0.02));
            BigDecimal juros = conta.getValorOriginal()
                    .multiply(BigDecimal.valueOf(0.0033))
                    .multiply(BigDecimal.valueOf(diasAtraso));

            conta.setValorMulta(multa);
            conta.setValorJuros(juros);
        }
    }

    private void criarEntradaFluxoCaixa(ContaReceber conta) {
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

    private void criarEntradaFluxoCaixa(ContaReceber conta, Usuario usuario) {
        FluxoCaixa fluxo = new FluxoCaixa();
        fluxo.setData(conta.getDataVencimento());
        fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.ENTRADA);
        fluxo.setCategoria(FluxoCaixa.CategoriaFluxo.VENDAS);
        fluxo.setDescricao("Previsão - " + conta.getDescricao());
        fluxo.setValor(conta.getValorTotal());
        fluxo.setStatus(FluxoCaixa.StatusFluxo.PREVISTO);
        fluxo.setContaReceber(conta);
        fluxo.setNumeroDocumento(conta.getNumeroDocumento());
        fluxo.setUsuarioCriacao(usuario);
        fluxo.setDataCriacao(LocalDateTime.now());

        fluxoCaixaRepository.save(fluxo);
    }

    private FluxoCaixa criarEntradaFluxoCaixaRecebimento(ContaReceber conta, BigDecimal valorRecebido,
            Usuario usuario) {
        FluxoCaixa fluxo = new FluxoCaixa();
        fluxo.setData(LocalDate.now());
        fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.ENTRADA);
        fluxo.setCategoria(FluxoCaixa.CategoriaFluxo.VENDAS);
        fluxo.setDescricao("Recebimento - " + conta.getDescricao());
        fluxo.setValor(valorRecebido);
        fluxo.setStatus(FluxoCaixa.StatusFluxo.REALIZADO);
        fluxo.setContaReceber(conta);
        fluxo.setNumeroDocumento(conta.getNumeroDocumento());
        fluxo.setUsuarioCriacao(usuario);
        fluxo.setDataCriacao(LocalDateTime.now());

        return fluxoCaixaRepository.save(fluxo);
    }

    // ================= HISTÓRICO =================
    @Transactional
    public void registrarHistorico(ContaReceber conta, Usuario usuario, String acao, String observacao) {
        HistoricoContaReceber historico = new HistoricoContaReceber();
        historico.setConta(conta);
        historico.setUsuario(usuario);
        historico.setAcao(acao);
        historico.setObservacao(observacao);
        historico.setNumeroDocumento(conta.getNumeroDocumento());
        historico.setDataHora(LocalDateTime.now());
        historicoReceberRepository.save(historico);
    }
}
