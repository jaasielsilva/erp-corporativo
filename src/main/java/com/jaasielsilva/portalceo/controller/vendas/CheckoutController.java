package com.jaasielsilva.portalceo.controller.vendas;

import com.jaasielsilva.portalceo.model.Pedido;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.service.PedidoService;
import com.jaasielsilva.portalceo.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/vendas")
public class CheckoutController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    

    @PostMapping("/pedidos")
    public String criarPedido(@ModelAttribute PedidoForm form, Model model) {
        List<PedidoService.ItemCriacao> itens = new ArrayList<>();
        for (ItemForm it : form.getItens()) {
            PedidoService.ItemCriacao ic = new PedidoService.ItemCriacao();
            ic.produtoId = it.getProduto() != null ? it.getProduto().getId() : null;
            ic.quantidade = it.getQuantidade();
            ic.precoUnitario = it.getPrecoUnitario();
            itens.add(ic);
        }

        Pedido pedido = pedidoService.criarPedido(form.getCliente(), itens);
        pedidoService.faturarPedido(pedido.getId(), form.getFormaRecebimento());
        return "redirect:/financeiro/contas-receber";
    }

    public static class PedidoForm {
        private Long cliente;
        private String formaRecebimento;
        private List<ItemForm> itens = new ArrayList<>();

        public Long getCliente() { return cliente; }
        public void setCliente(Long cliente) { this.cliente = cliente; }
        public String getFormaRecebimento() { return formaRecebimento; }
        public void setFormaRecebimento(String formaRecebimento) { this.formaRecebimento = formaRecebimento; }
        public List<ItemForm> getItens() { return itens; }
        public void setItens(List<ItemForm> itens) { this.itens = itens; }
    }

    public static class ItemForm {
        private Produto produto;
        private Integer quantidade;
        private BigDecimal precoUnitario;

        public Produto getProduto() { return produto; }
        public void setProduto(Produto produto) { this.produto = produto; }
        public Integer getQuantidade() { return quantidade; }
        public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
        public BigDecimal getPrecoUnitario() { return precoUnitario; }
        public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
    }
}