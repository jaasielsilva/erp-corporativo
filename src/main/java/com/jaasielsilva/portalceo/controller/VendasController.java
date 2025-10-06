package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vendas")
public class VendasController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "vendas/dashboard";
    }

    @GetMapping("")
    public String lista() {
        return "vendas/lista";
    }

    @GetMapping("/pdv")
    public String pdv() {
        return "vendas/pdv";
    }

    @GetMapping("/caixa")
    public String caixa() {
        return "vendas/caixa";
    }

    @GetMapping("/novo")
    public String novo() {
        return "vendas/novo";
    }

    @GetMapping("/clientes")
    public String clientes() {
        return "vendas/clientes";
    }

    @GetMapping("/produtos")
    public String produtos() {
        return "vendas/produtos";
    }

    @GetMapping("/relatorios")
    public String relatorios() {
        return "vendas/relatorios";
    }

    @GetMapping("/configuracoes")
    public String configuracoes() {
        return "vendas/configuracoes";
    }
}