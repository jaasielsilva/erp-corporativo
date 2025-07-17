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

import java.time.LocalDateTime;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService; // ✅ Faltava isso para buscar os produtos

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

    // 💾 SALVAR VENDA (com produtos fixos por enquanto)
    @PostMapping("/salvar")
    public String salvarVenda(@ModelAttribute Venda venda, @RequestParam Long clienteId, Model model) {
    Cliente cliente = clienteService.buscarPorId(clienteId)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

    venda.setCliente(cliente);
    venda.setDataVenda(LocalDateTime.now());

    Produto produto1 = produtoService.buscarPorId(1L).orElseThrow();
    Produto produto2 = produtoService.buscarPorId(2L).orElseThrow();

    VendaItem item1 = new VendaItem(produto1, 2, produto1.getPrecoVenda());
    VendaItem item2 = new VendaItem(produto2, 1, produto2.getPrecoVenda());

    venda.getItens().add(item1);
    venda.getItens().add(item2);

    vendaService.salvar(venda);

    return "redirect:/vendas";
}

}
