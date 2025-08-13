package com.jaasielsilva.portalceo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
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
    
    @Autowired
    private ColaboradorService colaboradorService; 

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        // Buscar o usuário logado
        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);

        // Verificar se é ADMIN (para mostrar menus ou seções específicas)
        boolean isAdmin = usuarioLogado != null && usuarioLogado.getPerfis().stream()
                                    .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

        // ===== DADOS PRINCIPAIS =====
        // Total de clientes ativos
        long totalClientes = clienteService.contarTotal();
        
        // Total de vendas (soma de valores)
        BigDecimal totalVendas = vendaService.getTotalVendas();
        
        // Crescimento de vendas vs mês anterior
        String crescimentoVendas = vendaService.calcularCrescimentoVendas();
        
        // Quantidade total de produtos no estoque
        long produtosEstoque = produtoService.somarQuantidadeEstoque();
        
        // Faturamento dos últimos 12 meses
        BigDecimal faturamentoMensal = vendaService.calcularFaturamentoUltimos12Meses();
        
        // Crescimento do faturamento vs mês anterior
        String crescimentoFaturamento = vendaService.calcularCrescimentoFaturamento();
        
        // Produtos críticos (produtos com estoque <= minimoEstoque)
        long produtosCriticos = produtoService.contarProdutosCriticos();
        
        // Total de funcionários (colaboradores ativos)
        long totalFuncionarios = colaboradorService.contarAtivos();
        
        // Contratações nos últimos 12 meses
        long contratacoes12Meses = colaboradorService.contarContratacaosPorPeriodo(12);
        
        // Solicitações pendentes (simulado)
        long solicitacoesPendentes = 47L;
        
        // Solicitações atrasadas (simulado)
        long solicitacoesAtrasadas = 12L;
        
        // Percentual da meta (simulado)
        String percentualMeta = "87%";

        // ===== GRÁFICOS DE VENDAS =====
        // Obter vendas dos últimos 12 meses para gráfico principal
        Map<YearMonth, BigDecimal> vendasUltimos12Meses = vendaService.getVendasUltimosMeses(12);
        
        List<String> ultimos12MesesLabels = new ArrayList<>();
        List<BigDecimal> ultimos12MesesValores = new ArrayList<>();
        
        vendasUltimos12Meses.forEach((ym, valor) -> {
            String label = ym.getMonth().name().substring(0, 3) + "/" + String.valueOf(ym.getYear()).substring(2);
            ultimos12MesesLabels.add(label);
            ultimos12MesesValores.add(valor);
        });
        
        // Meta de vendas mensal (simulado)
        List<BigDecimal> metaVendasMensal = new ArrayList<>();
        BigDecimal metaBase = faturamentoMensal.multiply(new BigDecimal("1.2"));
        for (int i = 0; i < 12; i++) {
            metaVendasMensal.add(metaBase);
        }

        // ===== DADOS PARA GRÁFICOS ADICIONAIS =====
        // Vendas por categoria (simulado)
        List<String> categoriasLabels = Arrays.asList(
            "Eletrônicos", "Roupas", "Casa & Jardim", "Esportes", "Livros", "Beleza"
        );
        List<BigDecimal> categoriasValores = Arrays.asList(
            new BigDecimal("450000"), new BigDecimal("320000"), new BigDecimal("280000"),
            new BigDecimal("180000"), new BigDecimal("120000"), new BigDecimal("95000")
        );
        
        // Status das solicitações (simulado)
        List<Integer> solicitacoesStatus = Arrays.asList(47, 234, 18, 23); // Pendentes, Aprovadas, Rejeitadas, Em Análise
        
        // Performance por área (simulado)
        List<Integer> performanceIndicadores = Arrays.asList(87, 92, 78, 85, 90); // Vendas, Atendimento, Logística, Qualidade, Financeiro

        // ===== MÉTRICAS FINANCEIRAS =====
        String margemLucro = "23.5%";
        String ticketMedio = "R$ 2.284";
        String roiMensal = "18.7%";
        String inadimplencia = "2.1%";
        
        // ===== MÉTRICAS DE RH =====
        String taxaRetencao = "94.2%";
        String produtividadeMedia = "87.3%";
        String horasExtras = "234h";
        String satisfacaoInterna = "8.7/10";
        
        // ===== MÉTRICAS OPERACIONAIS =====
        String giroEstoque = "4.2x";
        String tempoEntrega = "2.3 dias";
        String taxaDevolucao = "1.8%";
        String eficienciaLogistica = "91.5%";

        // ===== ADICIONANDO ATRIBUTOS AO MODEL =====
        // Dados principais
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("faturamentoMensal", String.format("R$ %,.0f", faturamentoMensal));
        model.addAttribute("crescimentoFaturamento", crescimentoFaturamento);
        model.addAttribute("totalVendas", String.format("%,d", totalVendas.longValue()));
        model.addAttribute("crescimentoVendas", crescimentoVendas);
        model.addAttribute("totalClientes", String.format("%,d", totalClientes));
        model.addAttribute("totalProdutos", String.format("%,d", produtosEstoque));
        model.addAttribute("totalFuncionarios", String.format("%,d", totalFuncionarios));
        model.addAttribute("contratacoes12Meses", contratacoes12Meses);
        model.addAttribute("solicitacoesPendentes", String.format("%,d", solicitacoesPendentes));
        model.addAttribute("produtosCriticos", String.format("%,d", produtosCriticos));
        model.addAttribute("solicitacoesAtrasadas", String.format("%,d", solicitacoesAtrasadas));
        model.addAttribute("percentualMeta", percentualMeta);
        
        // Dados para gráficos
        model.addAttribute("ultimos12MesesLabels", ultimos12MesesLabels);
        model.addAttribute("ultimos12MesesValores", ultimos12MesesValores);
        model.addAttribute("metaVendasMensal", metaVendasMensal);
        model.addAttribute("categoriasLabels", categoriasLabels);
        model.addAttribute("categoriasValores", categoriasValores);
        model.addAttribute("solicitacoesStatus", solicitacoesStatus);
        model.addAttribute("performanceIndicadores", performanceIndicadores);
        
        // Métricas de performance
        model.addAttribute("margemLucro", margemLucro);
        model.addAttribute("ticketMedio", ticketMedio);
        model.addAttribute("roiMensal", roiMensal);
        model.addAttribute("inadimplencia", inadimplencia);
        model.addAttribute("taxaRetencao", taxaRetencao);
        model.addAttribute("produtividadeMedia", produtividadeMedia);
        model.addAttribute("horasExtras", horasExtras);
        model.addAttribute("satisfacaoInterna", satisfacaoInterna);
        model.addAttribute("giroEstoque", giroEstoque);
        model.addAttribute("tempoEntrega", tempoEntrega);
        model.addAttribute("taxaDevolucao", taxaDevolucao);
        model.addAttribute("eficienciaLogistica", eficienciaLogistica);

        return "dashboard/index";
    }

}
