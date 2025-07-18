package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.VendaItem;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.VendaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    // 🧾 LISTAR VENDAS
    @GetMapping
    public String listar(
            @RequestParam(name = "cpfCnpj", required = false) String cpfCnpj,
            Model model) {

        var vendasFiltradas = vendaService.buscarPorCpfCnpj(cpfCnpj);
        model.addAttribute("vendas", vendasFiltradas);
        model.addAttribute("cpfCnpj", cpfCnpj);
        model.addAttribute("totalVendas", vendasFiltradas.size());
        model.addAttribute("valorTotalFormatado", vendaService.formatarValorTotal(vendasFiltradas));

        return "vendas/lista";
    }

    // ➕ FORMULÁRIO DE NOVA VENDA
    @GetMapping("/nova")
    public String novaVenda(Model model) {
        model.addAttribute("venda", new Venda());
        model.addAttribute("clientes", clienteService.buscarTodos());
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "vendas/cadastro";
    }

    // ✅ SALVAR VENDA COM PRODUTOS DINÂMICOS DO FORMULÁRIO
    @PostMapping("/salvar")
public String salvarVenda(@ModelAttribute Venda venda,
                         @RequestParam Long clienteId,
                         Model model) {

    Cliente cliente = clienteService.buscarPorId(clienteId)
        .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

    venda.setCliente(cliente);

    if (venda.getDataVenda() == null) {
        venda.setDataVenda(LocalDateTime.now());
    }

    // Vincula cada item à venda e calcula total
    BigDecimal total = BigDecimal.ZERO;
    for (VendaItem item : venda.getItens()) {
        item.setVenda(venda);

        // Para garantir: busca o produto completo no BD pelo id
        Produto produto = produtoService.buscarPorId(item.getProduto().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        item.setProduto(produto);

        // Somar subtotal: precoUnitario * quantidade
        BigDecimal subtotal = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
        total = total.add(subtotal);
    }

    venda.setTotal(total);

    vendaService.salvar(venda);

    return "redirect:/vendas";
}


}
