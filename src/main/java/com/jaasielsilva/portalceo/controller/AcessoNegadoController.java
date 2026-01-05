package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AcessoNegadoController {

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "error/403";
    }

    @RequestMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
