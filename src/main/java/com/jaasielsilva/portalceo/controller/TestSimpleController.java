package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestSimpleController {

    @GetMapping("/simple")
    public String testSimple(Model model) {
        model.addAttribute("message", "Test message");
        return "rh/colaboradores/adesao/inicio";
    }
    
    @GetMapping("/string")
    public String testString() {
        return "test-simple";
    }
}