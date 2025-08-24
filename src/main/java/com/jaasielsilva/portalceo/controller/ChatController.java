package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.*;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ChatService;
import com.jaasielsilva.portalceo.service.NotificacaoChatService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private NotificacaoChatService notificacaoChatService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Página principal do chat
     */
    @GetMapping
    public String chat(Model model, Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            List<ConversaDTO> conversas = chatService.buscarConversasDoUsuario(usuario.getId());
            model.addAttribute("conversas", conversas);
            model.addAttribute("usuarioAtual", usuario);
            return "chat/index";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    // ===== ENDPOINTS REST =====

    /**
     * Busca todas as conversas do usuário autenticado
     */
    @GetMapping("/api/conversas")
    @ResponseBody
    public ResponseEntity<List<ConversaDTO>> getConversas(Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            List<ConversaDTO> conversas = chatService.buscarConversasDoUsuario(usuario.getId());
            return ResponseEntity.ok(conversas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca mensagens de uma conversa específica
     */
    @GetMapping("/api/conversas/{conversaId}/mensagens")
    @ResponseBody
    public ResponseEntity<Page<MensagemDTO>> getMensagens(
            @PathVariable Long conversaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            Pageable pageable = PageRequest.of(page, size);
            Page<MensagemDTO> mensagens = chatService.buscarMensagensDaConversa(conversaId, usuario.getId(), pageable);
            return ResponseEntity.ok(mensagens);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Envia uma nova mensagem
     */
    @PostMapping("/api/mensagens")
    @ResponseBody
    public ResponseEntity<MensagemDTO> enviarMensagem(
            @RequestBody MensagemDTO mensagemDTO,
            Authentication authentication) {
        try {
            Usuario remetente = usuarioService.findByNome(authentication.getName());
            Mensagem.TipoMensagem tipoMensagem = mensagemDTO.getTipoMensagem() != null ? 
                Mensagem.TipoMensagem.valueOf(mensagemDTO.getTipoMensagem().toUpperCase()) : 
                Mensagem.TipoMensagem.TEXTO;
            
            MensagemDTO novaMensagem = chatService.enviarMensagem(
                remetente.getId(), 
                mensagemDTO.getDestinatarioId(), 
                mensagemDTO.getConteudo(),
                tipoMensagem
            );
            return ResponseEntity.ok(novaMensagem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marca mensagens como lidas
     */
    @PutMapping("/api/conversas/{conversaId}/marcar-lidas")
    @ResponseBody
    public ResponseEntity<Void> marcarMensagensComoLidas(
            @PathVariable Long conversaId,
            Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            chatService.marcarMensagensComoLidas(conversaId, usuario.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca usuários disponíveis para chat
     */
    @GetMapping("/api/usuarios")
    @ResponseBody
    public ResponseEntity<List<Usuario>> getUsuariosParaChat(
            @RequestParam(required = false) String busca,
            Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            List<Usuario> usuarios = chatService.buscarUsuariosParaChat(usuario.getId(), busca);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Inicia uma nova conversa
     */
    @PostMapping("/api/conversas")
    @ResponseBody
    public ResponseEntity<ConversaDTO> iniciarConversa(
            @RequestParam Long outroUsuarioId,
            Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            ConversaDTO conversa = chatService.buscarOuCriarConversa(usuario.getId(), outroUsuarioId);
            return ResponseEntity.ok(conversa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Conta mensagens não lidas
     */
    @GetMapping("/api/mensagens-nao-lidas/count")
    @ResponseBody
    public ResponseEntity<Long> contarMensagensNaoLidas(Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            Long count = chatService.contarMensagensNaoLidas(usuario.getId());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca notificações de chat
     */
    @GetMapping("/api/notificacoes")
    @ResponseBody
    public ResponseEntity<List<NotificacaoChatDTO>> getNotificacoes(
            @RequestParam(defaultValue = "false") boolean apenasNaoLidas,
            Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            List<NotificacaoChatDTO> notificacoes;
            
            if (apenasNaoLidas) {
                notificacoes = notificacaoChatService.buscarNotificacoesNaoLidas(usuario.getId());
            } else {
                notificacoes = notificacaoChatService.buscarNotificacoesDoUsuario(usuario.getId());
            }
            
            return ResponseEntity.ok(notificacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marca notificação como lida
     */
    @PutMapping("/api/notificacoes/{notificacaoId}/marcar-lida")
    @ResponseBody
    public ResponseEntity<Void> marcarNotificacaoComoLida(
            @PathVariable Long notificacaoId,
            Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            notificacaoChatService.marcarComoLida(notificacaoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Conta notificações não lidas
     */
    @GetMapping("/api/notificacoes/count")
    @ResponseBody
    public ResponseEntity<Long> contarNotificacoesNaoLidas(Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            Long count = notificacaoChatService.contarNotificacoesNaoLidas(usuario.getId());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
