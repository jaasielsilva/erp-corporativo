package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ClienteService clienteService;

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

    @GetMapping("/nova")
    public String novaVenda(Model model) {
        model.addAttribute("venda", new Venda());
        model.addAttribute("clientes", clienteService.buscarTodos());
        return "vendas/cadastro";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Venda venda) {
        vendaService.salvar(venda);
        return "redirect:/vendas";
    }
}
