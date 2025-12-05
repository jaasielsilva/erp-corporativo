package com.jaasielsilva.portalceo.controller.ajuda;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AjudaController {
    @GetMapping("/ajuda")
    public String ajudaIndex() {
        return "ajuda/index";
    }
}
