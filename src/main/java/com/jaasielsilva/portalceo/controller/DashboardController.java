package com.jaasielsilva.portalceo.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.VendaService;

@Controller
public class DashboardController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ProdutoService produtoService; 

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        // Buscar o usuário logado
        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);

        // Verificar se é ADMIN (para mostrar menus ou seções específicas)
        boolean isAdmin = usuarioLogado != null && usuarioLogado.getPerfis().stream()
                                    .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

        // Total de clientes ativos
        long totalClientes = clienteService.contarTotal();

        // Total de vendas (soma de valores)
        BigDecimal totalVendas = vendaService.getTotalVendas();

        // Quantidade total de produtos no estoque (soma de todas as quantidades)
        long produtosEstoque = produtoService.somarQuantidadeEstoque();

        // Últimas 2 vendas
        List<Venda> ultimasVendas = vendaService.buscarUltimasVendas(2);

        // Obter vendas dos últimos 5 meses para gráfico
        Map<YearMonth, BigDecimal> vendasUltimos5Meses = vendaService.getVendasUltimosMeses(5);

        // Preparar labels e valores para gráfico
        List<String> graficoLabels = new ArrayList<>();
        List<BigDecimal> graficoValores = new ArrayList<>();

        vendasUltimos5Meses.forEach((ym, valor) -> {
            String label = ym.getMonth().name().substring(0, 3) + "/" + ym.getYear(); // Exemplo: "JUL/2025"
            graficoLabels.add(label);
            graficoValores.add(valor);
        });

        // Adicionando atributos ao model
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalVendas", totalVendas);
        model.addAttribute("produtosEstoque", produtosEstoque);
        model.addAttribute("ultimasVendas", ultimasVendas);
        model.addAttribute("produtos", produtoService.listarTodosProdutos());

        model.addAttribute("graficoLabels", graficoLabels);
        model.addAttribute("graficoValores", graficoValores);

        return "dashboard/index";
    }

}
