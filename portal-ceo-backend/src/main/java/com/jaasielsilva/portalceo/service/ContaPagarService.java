package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.ContaPagar;
import com.jaasielsilva.portalceo.model.FluxoCaixa;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.ContaPagarRepository;
import com.jaasielsilva.portalceo.repository.FluxoCaixaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ContaPagarService {

    private static final Logger logger = LoggerFactory.getLogger(ContaPagarService.class);

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private FluxoCaixaRepository fluxoCaixaRepository;

    /**
     * Lista todas as contas a pagar
     */
    public List<ContaPagar> listarTodas() {
        return contaPagarRepository.findAll();
    }

    /**
     * Busca conta por ID
     */
    public Optional<ContaPagar> buscarPorId(Long id) {
        return contaPagarRepository.findById(id);
    }

    /**
     * Lista contas por status
     */
    public List<ContaPagar> listarPorStatus(ContaPagar.StatusContaPagar status) {
        return contaPagarRepository.findByStatusOrderByDataVencimento(status);
    }

    /**
     * Lista contas pendentes
     */
    public List<ContaPagar> listarPendentes() {
        return listarPorStatus(ContaPagar.StatusContaPagar.PENDENTE);
    }

    /**
     * Lista contas aprovadas
     */
    public List<ContaPagar> listarAprovadas() {
        return listarPorStatus(ContaPagar.StatusContaPagar.APROVADA);
    }

    /**
     * Lista contas vencidas
     */
    public List<ContaPagar> listarVencidas() {
        List<ContaPagar.StatusContaPagar> statuses = Arrays.asList(
            ContaPagar.StatusContaPagar.PENDENTE, 
            ContaPagar.StatusContaPagar.APROVADA
        );
        return contaPagarRepository.findVencidas(LocalDate.now(), statuses);
    }

    /**
     * Lista contas que vencem nos próximos dias
     */
    public List<ContaPagar> listarVencendoEm(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate futuro = hoje.plusDays(dias);
        return contaPagarRepository.findVencendoEm(hoje, futuro);
    }

    /**
     * Lista contas por período
     */
    public List<ContaPagar> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return contaPagarRepository.findByPeriodo(inicio, fim);
    }

    /**
     * Salva ou atualiza uma conta a pagar
     */
    @Transactional
    public ContaPagar salvar(ContaPagar contaPagar) {
        logger.info("Salvando conta a pagar: {} - Valor: {}", 
                   contaPagar.getDescricao(), contaPagar.getValorOriginal());

        // Validar se o número do documento já existe
        if (contaPagar.getNumeroDocumento() != null) {
            boolean existe = contaPagarRepository.existsByNumeroDocumento(contaPagar.getNumeroDocumento());
            if (existe && (contaPagar.getId() == null || 
                !contaPagarRepository.findById(contaPagar.getId())
                    .map(c -> c.getNumeroDocumento().equals(contaPagar.getNumeroDocumento()))
                    .orElse(false))) {
                throw new IllegalArgumentException("Já existe uma conta com este número de documento");
            }
        }

        return contaPagarRepository.save(contaPagar);
    }

    /**
     * Aprova uma conta a pagar
     */
    @Transactional
    public ContaPagar aprovar(Long id, Usuario usuarioAprovacao) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        if (conta.getStatus() != ContaPagar.StatusContaPagar.PENDENTE) {
            throw new IllegalStateException("Só é possível aprovar contas pendentes");
        }

        conta.setStatus(ContaPagar.StatusContaPagar.APROVADA);
        conta.setUsuarioAprovacao(usuarioAprovacao);
        conta.setDataAprovacao(LocalDate.now().atStartOfDay());

        logger.info("Conta a pagar aprovada: {} por {}", conta.getDescricao(), usuarioAprovacao.getEmail());

        return contaPagarRepository.save(conta);
    }

    /**
     * Efetua pagamento de uma conta
     */
    @Transactional
    public ContaPagar efetuarPagamento(Long id, BigDecimal valorPago, String formaPagamento, Usuario usuario) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        if (conta.getStatus() != ContaPagar.StatusContaPagar.APROVADA) {
            throw new IllegalStateException("Só é possível pagar contas aprovadas");
        }

        if (valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero");
        }

        // Atualizar conta
        conta.setValorPago(valorPago);
        conta.setFormaPagamento(formaPagamento);
        conta.setDataPagamento(LocalDate.now());
        conta.setStatus(ContaPagar.StatusContaPagar.PAGA);

        // Criar registro no fluxo de caixa
        FluxoCaixa fluxo = new FluxoCaixa();
        fluxo.setDescricao("Pagamento: " + conta.getDescricao());
        fluxo.setValor(valorPago);
        fluxo.setData(LocalDate.now());
        fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.SAIDA);
        fluxo.setCategoria(mapearCategoriaFluxo(conta.getCategoria()));
        fluxo.setStatus(FluxoCaixa.StatusFluxo.REALIZADO);
        fluxo.setNumeroDocumento(conta.getNumeroDocumento());
        fluxo.setContaPagar(conta);
        fluxo.setUsuarioCriacao(usuario);

        fluxoCaixaRepository.save(fluxo);

        logger.info("Pagamento efetuado: {} - Valor: {}", conta.getDescricao(), valorPago);

        return contaPagarRepository.save(conta);
    }

    /**
     * Cancela uma conta a pagar
     */
    @Transactional
    public ContaPagar cancelar(Long id, String motivo) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        if (conta.getStatus() == ContaPagar.StatusContaPagar.PAGA) {
            throw new IllegalStateException("Não é possível cancelar conta já paga");
        }

        conta.setStatus(ContaPagar.StatusContaPagar.CANCELADA);
        conta.setObservacoes((conta.getObservacoes() != null ? conta.getObservacoes() + " | " : "") + 
                           "CANCELADA: " + motivo);

        logger.info("Conta a pagar cancelada: {} - Motivo: {}", conta.getDescricao(), motivo);

        return contaPagarRepository.save(conta);
    }

    /**
     * Exclui uma conta a pagar
     */
    @Transactional
    public void excluir(Long id) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        if (conta.getStatus() == ContaPagar.StatusContaPagar.PAGA) {
            throw new IllegalStateException("Não é possível excluir conta já paga");
        }

        logger.info("Excluindo conta a pagar: {}", conta.getDescricao());
        contaPagarRepository.delete(conta);
    }

    /**
     * Calcula estatísticas das contas a pagar
     */
    public Map<String, Object> calcularEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        // Totais por status
        BigDecimal totalPendente = contaPagarRepository.sumValorTotalByStatus(ContaPagar.StatusContaPagar.PENDENTE);
        BigDecimal totalAprovada = contaPagarRepository.sumValorTotalByStatus(ContaPagar.StatusContaPagar.APROVADA);
        BigDecimal totalPaga = contaPagarRepository.sumValorTotalByStatus(ContaPagar.StatusContaPagar.PAGA);

        stats.put("totalPendente", totalPendente != null ? totalPendente : BigDecimal.ZERO);
        stats.put("totalAprovada", totalAprovada != null ? totalAprovada : BigDecimal.ZERO);
        stats.put("totalPaga", totalPaga != null ? totalPaga : BigDecimal.ZERO);

        // Contadores
        Long countPendente = contaPagarRepository.countByStatus(ContaPagar.StatusContaPagar.PENDENTE);
        Long countAprovada = contaPagarRepository.countByStatus(ContaPagar.StatusContaPagar.APROVADA);
        Long countVencida = contaPagarRepository.countVencidas(LocalDate.now());

        stats.put("countPendente", countPendente != null ? countPendente : 0L);
        stats.put("countAprovada", countAprovada != null ? countAprovada : 0L);
        stats.put("countVencida", countVencida != null ? countVencida : 0L);

        // Valores pagos no período
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate fimMes = inicioMes.plusMonths(1).minusDays(1);
        BigDecimal pagoMes = contaPagarRepository.sumValorPagoByPeriodo(inicioMes, fimMes);
        stats.put("pagoNoMes", pagoMes != null ? pagoMes : BigDecimal.ZERO);

        return stats;
    }

    /**
     * Busca contas por texto
     */
    public List<ContaPagar> buscarPorTexto(String texto) {
        return contaPagarRepository.findByDescricaoContainingIgnoreCase(texto);
    }

    /**
     * Mapeia categoria da conta para categoria do fluxo de caixa
     */
    private FluxoCaixa.CategoriaFluxo mapearCategoriaFluxo(ContaPagar.CategoriaContaPagar categoria) {
        return switch (categoria) {
            case FORNECEDORES -> FluxoCaixa.CategoriaFluxo.FORNECEDORES;
            case SALARIOS -> FluxoCaixa.CategoriaFluxo.SALARIOS;
            case IMPOSTOS -> FluxoCaixa.CategoriaFluxo.IMPOSTOS;
            case ALUGUEL -> FluxoCaixa.CategoriaFluxo.ALUGUEL;
            case ENERGIA -> FluxoCaixa.CategoriaFluxo.ENERGIA;
            case TELEFONE -> FluxoCaixa.CategoriaFluxo.TELEFONE;
            case COMBUSTIVEL -> FluxoCaixa.CategoriaFluxo.COMBUSTIVEL;
            case MANUTENCAO -> FluxoCaixa.CategoriaFluxo.MANUTENCAO;
            case MARKETING -> FluxoCaixa.CategoriaFluxo.MARKETING;
            case CONSULTORIA, OUTROS -> FluxoCaixa.CategoriaFluxo.OUTRAS_DESPESAS;
        };
    }

    /**
     * Calcula valor total a pagar (com juros, multas e descontos)
     */
    public BigDecimal calcularValorTotalAPagar() {
        List<ContaPagar.StatusContaPagar> statuses = Arrays.asList(
            ContaPagar.StatusContaPagar.PENDENTE,
            ContaPagar.StatusContaPagar.APROVADA,
            ContaPagar.StatusContaPagar.VENCIDA
        );

        return contaPagarRepository.findAll().stream()
                .filter(c -> statuses.contains(c.getStatus()))
                .map(ContaPagar::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}