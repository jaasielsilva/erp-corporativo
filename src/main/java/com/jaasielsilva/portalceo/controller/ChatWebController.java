package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chat-web")
public class ChatWebController {

    @GetMapping
    public String chatPage() {
        return "chat/index";
    }

    @GetMapping("/conversas/departamento/{departamentoId}")
    @ResponseBody
    public List<Map<String, Object>> getConversationsByDepartamento(@PathVariable Long departamentoId) {
        // TODO: Implementar a lógica para buscar conversas reais do banco de dados
        // Por enquanto, retorna uma lista vazia para evitar o erro 404
        System.out.println("Requisição para /chat-web/conversas/departamento/" + departamentoId);
        return Collections.emptyList();
    }
}