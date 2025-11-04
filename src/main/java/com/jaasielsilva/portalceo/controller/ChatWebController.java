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

    @GetMapping("/departamentos")
    public String departamentosPage() {
        return "chat/departamentos";
    }
}