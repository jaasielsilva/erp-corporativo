package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AcessoNegadoController {

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "error/acesso-negado";
    }
}
