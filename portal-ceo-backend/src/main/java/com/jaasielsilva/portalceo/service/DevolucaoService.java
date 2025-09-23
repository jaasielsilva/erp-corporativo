package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.DevolucaoRepository;
import com.jaasielsilva.portalceo.repository.VendaRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DevolucaoService {

    private final DevolucaoRepository devolucaoRepository;
    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueService estoqueService;

    // CRUD Operations
    public Devolucao save(Devolucao devolucao) {
        validarDevolucao(devolucao);
        
        if (devolucao.getId() == null) {
            devolucao.setDataCriacao(LocalDateTime.now());
            devolucao.setStatus(Devolucao.StatusDevolucao.PENDENTE);
        }
        
        // Calculate total amount
        if (devolucao.getItens() != null && !devolucao.getItens().isEmpty()) {
            BigDecimal total = devolucao.getItens().stream()
                .map(DevolucaoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            devolucao.setValorTotal(total);
        }
        
        return devolucaoRepository.save(devolucao);
    }

    @Transactional(readOnly = true)
    public Optional<Devolucao> findById(Long id) {
        return devolucaoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Devolucao> findAll() {
        return devolucaoRepository.findAll();
    }

    public void deleteById(Long id) {
        devolucaoRepository.findById(id).ifPresent(devolucao -> {
            if (devolucao.isProcessada() || devolucao.isFinalizada()) {
                throw new IllegalStateException("Não é possível excluir uma devolução já processada");
            }
            devolucaoRepository.deleteById(id);
        });
    }

    // Business Operations
    @Transactional
    public Devolucao aprovarDevolucao(Long id, String observacoes, Usuario usuarioAutorizacao) {
        Devolucao devolucao = devolucaoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Devolução não encontrada"));

        if (!devolucao.podeSerAprovada()) {
            throw new IllegalStateException("Devolução não pode ser aprovada no status atual: " + devolucao.getStatus());
        }

        devolucao.setStatus(Devolucao.StatusDevolucao.APROVADA);
        devolucao.setUsuarioAutorizacao(usuarioAutorizacao);
        devolucao.setDataAutorizacao(LocalDateTime.now());
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            devolucao.setObservacoes(observacoes);
        }

        // Approve all items
        if (devolucao.getItens() != null) {
            devolucao.getItens().forEach(item -> item.setStatus(DevolucaoItem.StatusItem.APROVADO));
        }

        return devolucaoRepository.save(devolucao);
    }

    @Transactional
    public Devolucao rejeitarDevolucao(Long id, String motivoRejeicao, Usuario usuarioAutorizacao) {
        Devolucao devolucao = devolucaoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Devolução não encontrada"));

        if (!devolucao.podeSerAprovada()) {
            throw new IllegalStateException("Devolução não pode ser rejeitada no status atual: " + devolucao.getStatus());
        }

        devolucao.setStatus(Devolucao.StatusDevolucao.REJEITADA);
        devolucao.setUsuarioAutorizacao(usuarioAutorizacao);
        devolucao.setDataAutorizacao(LocalDateTime.now());
        devolucao.setObservacoes(motivoRejeicao);

        // Reject all items
        if (devolucao.getItens() != null) {
            devolucao.getItens().forEach(item -> item.setStatus(DevolucaoItem.StatusItem.REJEITADO));
        }

        return devolucaoRepository.save(devolucao);
    }

    @Transactional
    public Devolucao processarDevolucao(Long id, Usuario usuarioResponsavel) {
        Devolucao devolucao = devolucaoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Devolução não encontrada"));

        if (!devolucao.podeSerProcessada()) {
            throw new IllegalStateException("Devolução não pode ser processada no status atual: " + devolucao.getStatus());
        }

        // Calculate refund amount (may include return fee)
        BigDecimal valorEstorno = devolucao.getValorTotal().subtract(devolucao.getTaxaDevolucao());
        devolucao.setValorEstorno(valorEstorno);

        // Reintegrate stock for approved items
        if (devolucao.getItens() != null) {
            for (DevolucaoItem item : devolucao.getItens()) {
                if (item.podeReintegrarEstoque()) {
                    reintegrarEstoque(item);
                    item.setReintegrouEstoque(true);
                }
                item.setStatus(DevolucaoItem.StatusItem.PROCESSADO);
            }
        }

        devolucao.setStatus(Devolucao.StatusDevolucao.PROCESSADA);
        devolucao.setUsuarioResponsavel(usuarioResponsavel);
        devolucao.setDataProcessamento(LocalDateTime.now());

        return devolucaoRepository.save(devolucao);
    }

    @Transactional
    public Devolucao finalizarDevolucao(Long id, String observacoesFinal) {
        Devolucao devolucao = devolucaoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Devolução não encontrada"));

        if (!devolucao.isProcessada()) {
            throw new IllegalStateException("Devolução deve estar processada para ser finalizada");
        }

        devolucao.setStatus(Devolucao.StatusDevolucao.FINALIZADA);
        if (observacoesFinal != null && !observacoesFinal.trim().isEmpty()) {
            devolucao.setObservacoes(devolucao.getObservacoes() + "\n" + observacoesFinal);
        }

        return devolucaoRepository.save(devolucao);
    }

    @Transactional
    public Devolucao cancelarDevolucao(Long id, String motivoCancelamento, Usuario usuario) {
        Devolucao devolucao = devolucaoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Devolução não encontrada"));

        if (!devolucao.podeSerCancelada()) {
            throw new IllegalStateException("Devolução não pode ser cancelada no status atual: " + devolucao.getStatus());
        }

        devolucao.setStatus(Devolucao.StatusDevolucao.CANCELADA);
        devolucao.setObservacoes(motivoCancelamento);
        devolucao.setUsuarioResponsavel(usuario);

        return devolucaoRepository.save(devolucao);
    }

    // Query Operations
    @Transactional(readOnly = true)
    public List<Devolucao> findByStatus(Devolucao.StatusDevolucao status) {
        return devolucaoRepository.findByStatusOrderByDataDevolucaoDesc(status);
    }

    @Transactional(readOnly = true)
    public List<Devolucao> findByVenda(Venda venda) {
        return devolucaoRepository.findByVendaOriginalOrderByDataDevolucaoDesc(venda);
    }

    @Transactional(readOnly = true)
    public List<Devolucao> findByCliente(Cliente cliente) {
        return devolucaoRepository.findByVendaOriginal_ClienteOrderByDataDevolucaoDesc(cliente);
    }

    @Transactional(readOnly = true)
    public List<Devolucao> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return devolucaoRepository.findByDataDevolucaoBetweenOrderByDataDevolucaoDesc(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<Devolucao> findPendentes() {
        return devolucaoRepository.findByStatusOrderByDataDevolucaoDesc(Devolucao.StatusDevolucao.PENDENTE);
    }

    @Transactional(readOnly = true)
    public List<Devolucao> findPendentesAntigas(int diasLimite) {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(diasLimite);
        return devolucaoRepository.findDevolucoesPendentesAntigas(dataLimite);
    }

    // Statistical Reports
    @Transactional(readOnly = true)
    public Map<Devolucao.StatusDevolucao, Long> getEstatisticasPorStatus() {
        return List.of(Devolucao.StatusDevolucao.values())
            .stream()
            .collect(Collectors.toMap(
                status -> status,
                status -> devolucaoRepository.countByStatus(status)
            ));
    }

    @Transactional(readOnly = true)
    public Map<Devolucao.MotivoDevolucao, Long> getEstatisticasPorMotivo() {
        List<Object[]> results = devolucaoRepository.countByMotivo();
        return results.stream()
            .collect(Collectors.toMap(
                row -> (Devolucao.MotivoDevolucao) row[0],
                row -> (Long) row[1]
            ));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularValorTotalDevolucoes(LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal valor = devolucaoRepository.sumValorEstornoByPeriodo(inicio, fim);
        return valor != null ? valor : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getClientesComMaisDevolucoes(LocalDateTime inicio, LocalDateTime fim) {
        return devolucaoRepository.getClientesComMaisDevolucoes(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getProdutosComMaisDevolucoes(LocalDateTime inicio, LocalDateTime fim) {
        return devolucaoRepository.getProdutosComMaisDevolucoes(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getDevolucoesPorDia(LocalDateTime inicio, LocalDateTime fim) {
        return devolucaoRepository.getDevolucoesPorDia(inicio, fim);
    }

    @Transactional(readOnly = true)
    public Double getTempoMedioProcessamento() {
        return devolucaoRepository.getTempoMedioProcessamento();
    }

    // Business validation methods
    private void validarDevolucao(Devolucao devolucao) {
        if (devolucao.getVendaOriginal() == null) {
            throw new IllegalArgumentException("Venda original é obrigatória");
        }

        if (devolucao.getMotivo() == null) {
            throw new IllegalArgumentException("Motivo da devolução é obrigatório");
        }

        // Check if there's already a pending/approved return for this sale
        if (devolucao.getId() == null) {
            boolean jaExiste = devolucaoRepository.existsByVendaOriginalAndStatus(
                devolucao.getVendaOriginal(), Devolucao.StatusDevolucao.PENDENTE) ||
                devolucaoRepository.existsByVendaOriginalAndStatus(
                devolucao.getVendaOriginal(), Devolucao.StatusDevolucao.APROVADA);
            
            if (jaExiste) {
                throw new IllegalArgumentException("Já existe uma devolução pendente ou aprovada para esta venda");
            }
        }

        // Validate return period (example: 30 days)
        if (devolucao.isForaPrazo(30)) {
            throw new IllegalArgumentException("Prazo para devolução expirado (máximo 30 dias)");
        }
    }

    private void reintegrarEstoque(DevolucaoItem item) {
        try {
            Produto produto = item.getProduto();
            int novaQuantidade = produto.getEstoque() + item.getQuantidade();
            produto.setEstoque(novaQuantidade);
            produtoRepository.save(produto);
            
            // Log stock movement if EstoqueService supports it
            // estoqueService.registrarMovimentacao(produto, item.getQuantidade(), "DEVOLUCAO", "Reintegração por devolução");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao reintegrar estoque: " + e.getMessage(), e);
        }
    }

    // Factory method to create return from sale
    public Devolucao criarDevolucaoDeVenda(Long vendaId, Devolucao.MotivoDevolucao motivo, 
                                          String observacoes, Usuario usuario) {
        Venda venda = vendaRepository.findById(vendaId)
            .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));

        Devolucao devolucao = new Devolucao();
        devolucao.setVendaOriginal(venda);
        devolucao.setMotivo(motivo);
        devolucao.setObservacoes(observacoes);
        devolucao.setUsuarioResponsavel(usuario);
        devolucao.setTipo(Devolucao.TipoDevolucao.TOTAL);

        // Create return items from sale items
        List<DevolucaoItem> itens = venda.getItens().stream()
            .map(vendaItem -> {
                DevolucaoItem item = new DevolucaoItem();
                item.setDevolucao(devolucao);
                item.setProduto(vendaItem.getProduto());
                item.setVendaItemOriginal(vendaItem);
                item.setQuantidade(vendaItem.getQuantidade());
                item.setQuantidadeOriginal(vendaItem.getQuantidade());
                item.setPrecoUnitario(vendaItem.getPrecoUnitario());
                item.setCondicaoProduto(DevolucaoItem.CondicaoProduto.PERFEITO);
                return item;
            })
            .collect(Collectors.toList());

        devolucao.setItens(itens);
        
        return devolucao;
    }
}