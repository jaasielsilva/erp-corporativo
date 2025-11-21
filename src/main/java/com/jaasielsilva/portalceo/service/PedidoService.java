package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.PedidoRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final ContaReceberService contaReceberService;

    public Pedido criarPedido(Long clienteId, List<ItemCriacao> itens) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow();
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);

        for (ItemCriacao item : itens) {
            Produto produto = produtoRepository.findById(item.produtoId).orElseThrow();
            PedidoItem pi = new PedidoItem();
            pi.setPedido(pedido);
            pi.setProduto(produto);
            pi.setQuantidade(item.quantidade);
            pi.setPrecoUnitario(item.precoUnitario);
            pedido.getItens().add(pi);
        }

        pedido.calcularTotal();
        return pedidoRepository.save(pedido);
    }

    public Pedido faturarPedido(Long pedidoId, String formaRecebimento) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        for (PedidoItem item : pedido.getItens()) {
            movimentacaoEstoqueService.registrarMovimentacao(
                    item.getProduto().getId(),
                    item.getQuantidade(),
                    TipoMovimentacao.SAIDA,
                    "VENDA",
                    "PDV"
            );
        }

        pedido.calcularTotal();
        pedido.setStatus(Pedido.Status.FATURADO);
        pedidoRepository.save(pedido);

        ContaReceber conta = new ContaReceber();
        conta.setDescricao("Pedido " + pedido.getId());
        conta.setCliente(pedido.getCliente());
        conta.setValorOriginal(pedido.getTotal());
        conta.setDataVencimento(LocalDate.now());
        conta.setDataEmissao(LocalDate.now());
        conta.setCategoria(ContaReceber.CategoriaContaReceber.PRODUTO);
        conta.setTipo(ContaReceber.TipoContaReceber.VENDA);
        conta.setFormaRecebimento(formaRecebimento);
        contaReceberService.save(conta);

        return pedido;
    }

    public static class ItemCriacao {
        public Long produtoId;
        public Integer quantidade;
        public BigDecimal precoUnitario;
    }
}