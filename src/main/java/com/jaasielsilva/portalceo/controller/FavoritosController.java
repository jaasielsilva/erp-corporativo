package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/favoritos")
public class FavoritosController {
    /**
     * Exibe a página de favoritos.
     *
     * @param model o modelo para a view
     * @return o nome da view de favoritos
     */
    @GetMapping
    public String favoritos(Model model) {      
        return "favoritos/index";
    }
}