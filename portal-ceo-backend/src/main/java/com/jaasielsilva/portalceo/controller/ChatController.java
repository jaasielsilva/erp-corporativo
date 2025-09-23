package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ChatService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para o módulo de Chat
 * 
 * @author Sistema ERP
 * @version 2.0
 */
@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    // Mostrar chat principal
    @GetMapping
    public String index(Model model) {
        try {
            // Obter usuário logado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                String username = auth.getName();
                Usuario usuarioLogado = usuarioService.buscarPorEmail(username).orElse(null);
                
                if (usuarioLogado != null) {
                    // Adicionar dados do usuário ao modelo
                    model.addAttribute("usuarioLogado", usuarioLogado);
                    model.addAttribute("usuarioId", usuarioLogado.getId());
                    model.addAttribute("usuarioNome", usuarioLogado.getNome());
                    
                    // Buscar conversas do usuário
                    model.addAttribute("conversas", chatService.buscarConversasDoUsuario(usuarioLogado.getId()));
                    
                    // Contar mensagens não lidas
                    model.addAttribute("mensagensNaoLidas", chatService.contarMensagensNaoLidas(usuarioLogado.getId()));
                }
            }
            
        } catch (Exception e) {
            // Log do erro e continuar com dados básicos
            System.err.println("Erro ao carregar dados do chat: " + e.getMessage());
            model.addAttribute("erro", "Erro ao carregar dados do chat");
        }
        
        // Configurações da página
        model.addAttribute("pageTitle", "Chat");
        model.addAttribute("pageSubtitle", "Comunicação em tempo real");
        model.addAttribute("moduleIcon", "fas fa-comments");
        model.addAttribute("moduleCSS", "chat");
        
        return "chat/index";
    }
    
    /**
     * Página de conversa específica
     */
    @GetMapping("/conversa")
    public String conversa(@RequestParam("id") Long conversaId, Model model) {
        try {
            // Obter usuário logado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                String username = auth.getName();
                Usuario usuarioLogado = usuarioService.buscarPorEmail(username).orElse(null);
                
                if (usuarioLogado != null) {
                    // Verificar se usuário tem acesso à conversa
                    if (chatService.isParticipante(conversaId, usuarioLogado.getId())) {
                        model.addAttribute("conversaId", conversaId);
                        model.addAttribute("conversa", chatService.buscarConversaPorId(conversaId));
                        model.addAttribute("mensagens", chatService.buscarMensagensConversa(conversaId));
                        model.addAttribute("participantes", chatService.buscarParticipantesAtivos(conversaId));
                        model.addAttribute("usuarioLogado", usuarioLogado);
                    } else {
                        return "redirect:/chat?erro=acesso_negado";
                    }
                } else {
                    return "redirect:/chat?erro=usuario_nao_encontrado";
                }
            } else {
                return "redirect:/login";
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar conversa: " + e.getMessage());
            return "redirect:/chat?erro=conversa_nao_encontrada";
        }
        
        // Configurações da página
        model.addAttribute("pageTitle", "Chat - Conversa");
        model.addAttribute("pageSubtitle", "Conversa Ativa");
        model.addAttribute("moduleIcon", "fas fa-comment-dots");
        model.addAttribute("moduleCSS", "chat");
        
        return "chat/conversa";
    }
    
    /**
     * Página de configurações do chat
     */
    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Chat - Configurações");
        model.addAttribute("pageSubtitle", "Configurações do Sistema de Chat");
        model.addAttribute("moduleIcon", "fas fa-cog");
        model.addAttribute("moduleCSS", "chat");
        
        return "chat/configuracoes";
    }
}
