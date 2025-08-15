package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {

    // Página principal do Financeiro
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Financeiro");
        model.addAttribute("moduleCSS", "financeiro");
        return "financeiro/index";
    }

    // Contas a Pagar
    @GetMapping("/contas-pagar")
    public String contasPagar(Model model) {
        // TODO: Implementar lógica de contas a pagar
        // Buscar contas pendentes, vencidas, pagas
        // Adicionar filtros por período, fornecedor, status
        return "financeiro/contas-pagar";
    }

    // Contas a Receber
    @GetMapping("/contas-receber")
    public String contasReceber(Model model) {
        // TODO: Implementar lógica de contas a receber
        // Buscar contas pendentes, vencidas, recebidas
        // Adicionar filtros por período, cliente, status
        return "financeiro/contas-receber";
    }

    // Fluxo de Caixa
    @GetMapping("/fluxo-caixa")
    public String fluxoCaixa(Model model) {
        // TODO: Implementar lógica de fluxo de caixa
        // Exibir entradas, saídas, saldo atual
        // Gráficos de evolução financeira
        return "financeiro/fluxo-caixa";
    }

    // Transferências
    @GetMapping("/transferencias")
    public String transferencias(Model model) {
        // TODO: Implementar lógica de transferências
        // Transferências entre contas, bancos
        // Histórico de transferências
        return "financeiro/transferencias";
    }
}