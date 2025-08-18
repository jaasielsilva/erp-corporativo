package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.ChatMessage;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final UsuarioRepository usuarioRepository;

    public ChatController(ChatService chatService, UsuarioRepository usuarioRepository) {
        this.chatService = chatService;
        this.usuarioRepository = usuarioRepository;
    }

    // Página principal do chat
    @GetMapping
    public String chat(Model model) {
        model.addAttribute("messages", chatService.getTodasMensagens());
        return "chat/index"; // template Thymeleaf
    }

    // Recebe mensagens do cliente via WebSocket
    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {
        // Salva a mensagem no banco
        return chatService.salvarMensagem(
                message.getSender(),
                message.getContent(),
                message.getDestinatarioId()
        );
    }

    // Lista usuários com status online/offline (precisa implementar a lógica de online)
    @GetMapping("/usuarios")
    @ResponseBody
    public List<UsuarioDTO> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(u -> new UsuarioDTO(
                        u.getId(),
                        u.getNome(),
                        u.isOnline() // você precisa adicionar este campo ou calcular
                ))
                .collect(Collectors.toList());
    }

    // DTO para envio via JSON
    public static class UsuarioDTO {
        private Long id;
        private String nome;
        private boolean online;

        public UsuarioDTO(Long id, String nome, boolean online) {
            this.id = id;
            this.nome = nome;
            this.online = online;
        }

        public Long getId() { return id; }
        public String getNome() { return nome; }
        public boolean isOnline() { return online; }
    }
}
