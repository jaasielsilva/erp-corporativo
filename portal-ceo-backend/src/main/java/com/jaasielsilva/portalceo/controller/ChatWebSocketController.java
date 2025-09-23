package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Mensagem;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ChatService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket Controller para o Sistema de Chat Interno
 * Gerencia comunicação em tempo real via STOMP/WebSocket
 * 
 * @author Sistema ERP
 * @version 2.0
 */
@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ==================== MENSAGENS ====================
    
    /**
     * Recebe e processa mensagens enviadas via WebSocket
     * Rota: /app/chat.enviarMensagem
     */
    @MessageMapping("/chat.enviarMensagem")
    public void enviarMensagem(
            @Payload Map<String, Object> mensagemData,
            Principal principal) {
        try {
            // Extrair dados da mensagem
            Long conversaId = Long.valueOf(mensagemData.get("conversaId").toString());
            String conteudo = mensagemData.get("conteudo").toString();
            
            // Buscar usuário remetente
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            
            // Verificar se usuário é participante da conversa
            if (!chatService.isParticipante(conversaId, usuario.getId())) {
                // Enviar erro para o usuário
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", "Acesso negado à conversa")
                );
                return;
            }
            
            // Enviar mensagem
            Mensagem mensagem = chatService.enviarMensagem(conversaId, usuario.getId(), conteudo);
            
            // A notificação já é feita internamente pelo ChatService
            
        } catch (Exception e) {
            // Enviar erro para o usuário
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                Map.of("error", "Erro ao enviar mensagem: " + e.getMessage())
            );
        }
    }
    
    /**
     * Marca mensagem como lida via WebSocket
     * Rota: /app/chat.marcarLida
     */
    @MessageMapping("/chat.marcarLida")
    public void marcarMensagemLida(
            @Payload Map<String, Object> data,
            Principal principal) {
        try {
            Long mensagemId = Long.valueOf(data.get("mensagemId").toString());
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            
            chatService.marcarMensagensDaConversaComoLidas(mensagemId, usuario.getId());
            
            // Notificar remetente que mensagem foi lida
            messagingTemplate.convertAndSend(
                "/topic/conversa." + data.get("conversaId"),
                Map.of(
                    "tipo", "MENSAGEM_LIDA",
                    "mensagemId", mensagemId,
                    "leitoPor", usuario.getNome()
                )
            );
            
        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                Map.of("error", "Erro ao marcar mensagem como lida")
            );
        }
    }
    
    /**
     * Indica que usuário está digitando
     * Rota: /app/chat.digitando
     */
    @MessageMapping("/chat.digitando")
    public void usuarioDigitando(
            @Payload Map<String, Object> data,
            Principal principal) {
        try {
            Long conversaId = Long.valueOf(data.get("conversaId").toString());
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            
            // Verificar se usuário é participante
            if (!chatService.isParticipante(conversaId, usuario.getId())) {
                return;
            }
            
            // Notificar outros participantes
            messagingTemplate.convertAndSend(
                "/topic/conversa." + conversaId,
                Map.of(
                    "tipo", "USUARIO_DIGITANDO",
                    "usuario", usuario.getNome(),
                    "usuarioId", usuario.getId()
                )
            );
            
        } catch (Exception e) {
            // Log do erro (não enviar para usuário para evitar spam)
            System.err.println("Erro ao processar indicação de digitação: " + e.getMessage());
        }
    }
    
    /**
     * Indica que usuário parou de digitar
     * Rota: /app/chat.pararDigitar
     */
    @MessageMapping("/chat.pararDigitar")
    public void usuarioParouDigitar(
            @Payload Map<String, Object> data,
            Principal principal) {
        try {
            Long conversaId = Long.valueOf(data.get("conversaId").toString());
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            
            // Verificar se usuário é participante
            if (!chatService.isParticipante(conversaId, usuario.getId())) {
                return;
            }
            
            // Notificar outros participantes
            messagingTemplate.convertAndSend(
                "/topic/conversa." + conversaId,
                Map.of(
                    "tipo", "USUARIO_PAROU_DIGITAR",
                    "usuario", usuario.getNome(),
                    "usuarioId", usuario.getId()
                )
            );
            
        } catch (Exception e) {
            System.err.println("Erro ao processar parada de digitação: " + e.getMessage());
        }
    }
    
    // ==================== PRESENÇA ====================
    
    /**
     * Atualiza status online do usuário
     * Rota: /app/chat.online
     */
    @MessageMapping("/chat.online")
    public void usuarioOnline(Principal principal) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            chatService.atualizarStatusOnline(usuario.getId(), true);
            
            // Notificar contatos sobre status online
            messagingTemplate.convertAndSend(
                "/topic/usuarios.status",
                Map.of(
                    "tipo", "USUARIO_ONLINE",
                    "usuarioId", usuario.getId(),
                    "usuario", usuario.getNome()
                )
            );
            
        } catch (Exception e) {
            System.err.println("Erro ao atualizar status online: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza status offline do usuário
     * Rota: /app/chat.offline
     */
    @MessageMapping("/chat.offline")
    public void usuarioOffline(Principal principal) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            chatService.atualizarStatusOnline(usuario.getId(), false);
            
            // Notificar contatos sobre status offline
            messagingTemplate.convertAndSend(
                "/topic/usuarios.status",
                Map.of(
                    "tipo", "USUARIO_OFFLINE",
                    "usuarioId", usuario.getId(),
                    "usuario", usuario.getNome()
                )
            );
            
        } catch (Exception e) {
            System.err.println("Erro ao atualizar status offline: " + e.getMessage());
        }
    }
    
    // ==================== CONVERSAS ====================
    
    /**
     * Usuário entra em uma conversa
     * Rota: /app/chat.entrarConversa
     */
    @MessageMapping("/chat.entrarConversa")
    public void entrarConversa(
            @Payload Map<String, Object> data,
            Principal principal) {
        try {
            Long conversaId = Long.valueOf(data.get("conversaId").toString());
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            
            // Verificar se usuário é participante
            if (!chatService.isParticipante(conversaId, usuario.getId())) {
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", "Acesso negado à conversa")
                );
                return;
            }
            
            // Atualizar última visualização
            chatService.atualizarUltimaVisualizacao(conversaId, usuario.getId());
            
            // Notificar outros participantes
            messagingTemplate.convertAndSend(
                "/topic/conversa." + conversaId,
                Map.of(
                    "tipo", "USUARIO_ENTROU",
                    "usuario", usuario.getNome(),
                    "usuarioId", usuario.getId()
                )
            );
            
        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                Map.of("error", "Erro ao entrar na conversa")
            );
        }
    }
    
    /**
     * Usuário sai de uma conversa
     * Rota: /app/chat.sairConversa
     */
    @MessageMapping("/chat.sairConversa")
    public void sairConversa(
            @Payload Map<String, Object> data,
            Principal principal) {
        try {
            Long conversaId = Long.valueOf(data.get("conversaId").toString());
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            
            // Notificar outros participantes
            messagingTemplate.convertAndSend(
                "/topic/conversa." + conversaId,
                Map.of(
                    "tipo", "USUARIO_SAIU",
                    "usuario", usuario.getNome(),
                    "usuarioId", usuario.getId()
                )
            );
            
        } catch (Exception e) {
            System.err.println("Erro ao sair da conversa: " + e.getMessage());
        }
    }
}