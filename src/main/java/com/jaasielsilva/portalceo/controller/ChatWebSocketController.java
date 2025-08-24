package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.DigitandoEventoDTO;
import com.jaasielsilva.portalceo.dto.MensagemDTO;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ChatService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Armazena usuários online
    private final Map<String, String> usuariosOnline = new ConcurrentHashMap<>();

    /**
     * Recebe mensagens via WebSocket e as envia para o destinatário
     */
    @MessageMapping("/chat.enviarMensagem")
    public void enviarMensagem(@Payload MensagemDTO mensagem, Principal principal) {
        try {
            Usuario remetente = usuarioService.findByNome(principal.getName());
            
            // Envia a mensagem através do serviço
            MensagemDTO novaMensagem = chatService.enviarMensagem(
                remetente.getId(),
                mensagem.getDestinatarioId(),
                mensagem.getConteudo(),
                mensagem.getTipoMensagem()
            );

            // Envia para o destinatário via WebSocket
            messagingTemplate.convertAndSendToUser(
                String.valueOf(mensagem.getDestinatarioId()),
                "/queue/mensagens",
                novaMensagem
            );

            // Confirma para o remetente
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/mensagens.confirmacao",
                novaMensagem
            );

        } catch (Exception e) {
            // Envia erro para o remetente
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/erros",
                Map.of("erro", "Erro ao enviar mensagem: " + e.getMessage())
            );
        }
    }

    /**
     * Gerencia eventos de digitação
     */
    @MessageMapping("/chat.digitando")
    public void gerenciarDigitacao(@Payload DigitandoEventoDTO evento, Principal principal) {
        try {
            Usuario usuario = usuarioService.findByNome(principal.getName());
            evento.setUsuarioId(usuario.getId());
            evento.setUsuarioNome(usuario.getNome());
            evento.setTimestamp(System.currentTimeMillis());

            // Envia evento de digitação para todos os participantes da conversa
            messagingTemplate.convertAndSend(
                "/topic/conversa." + evento.getConversaId() + ".digitando",
                evento
            );

        } catch (Exception e) {
            // Log do erro (pode implementar logger)
            System.err.println("Erro ao processar evento de digitação: " + e.getMessage());
        }
    }

    /**
     * Marca mensagens como lidas via WebSocket
     */
    @MessageMapping("/chat.marcarLidas/{conversaId}")
    public void marcarMensagensComoLidas(
            @DestinationVariable Long conversaId,
            Principal principal) {
        try {
            Usuario usuario = usuarioService.findByNome(principal.getName());
            chatService.marcarMensagensComoLidas(conversaId, usuario.getId());

            // Notifica que as mensagens foram lidas
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/mensagens.lidas",
                Map.of("conversaId", conversaId, "sucesso", true)
            );

            // Notifica o outro participante da conversa
            messagingTemplate.convertAndSend(
                "/topic/conversa." + conversaId + ".lidas",
                Map.of("usuarioId", usuario.getId(), "conversaId", conversaId)
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/erros",
                Map.of("erro", "Erro ao marcar mensagens como lidas: " + e.getMessage())
            );
        }
    }

    /**
     * Gerencia conexões de usuários (entrada no chat)
     */
    @MessageMapping("/chat.entrar")
    public void entrarNoChat(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Usuario usuario = usuarioService.findByNome(principal.getName());
            
            // Adiciona usuário à lista de online
            usuariosOnline.put(principal.getName(), usuario.getId().toString());
            
            // Adiciona atributo na sessão WebSocket
            headerAccessor.getSessionAttributes().put("usuario", usuario.getNome());
            headerAccessor.getSessionAttributes().put("usuarioId", usuario.getId());

            // Notifica outros usuários que este usuário está online
            messagingTemplate.convertAndSend(
                "/topic/usuarios.online",
                Map.of(
                    "usuarioId", usuario.getId(),
                    "usuarioNome", usuario.getNome(),
                    "status", "online",
                    "timestamp", System.currentTimeMillis()
                )
            );

        } catch (Exception e) {
            System.err.println("Erro ao processar entrada no chat: " + e.getMessage());
        }
    }

    /**
     * Gerencia desconexões de usuários (saída do chat)
     */
    @MessageMapping("/chat.sair")
    public void sairDoChat(Principal principal) {
        try {
            if (principal != null) {
                Usuario usuario = usuarioService.findByNome(principal.getName());
                
                // Remove usuário da lista de online
                usuariosOnline.remove(principal.getName());

                // Notifica outros usuários que este usuário está offline
                messagingTemplate.convertAndSend(
                    "/topic/usuarios.online",
                    Map.of(
                        "usuarioId", usuario.getId(),
                        "usuarioNome", usuario.getNome(),
                        "status", "offline",
                        "timestamp", System.currentTimeMillis()
                    )
                );
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar saída do chat: " + e.getMessage());
        }
    }

    /**
     * Busca usuários online
     */
    @MessageMapping("/chat.usuariosOnline")
    public void buscarUsuariosOnline(Principal principal) {
        try {
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/usuarios.online",
                usuariosOnline
            );
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuários online: " + e.getMessage());
        }
    }

    /**
     * Envia notificação de nova conversa
     */
    public void notificarNovaConversa(Long usuarioId, Long conversaId) {
        try {
            messagingTemplate.convertAndSendToUser(
                String.valueOf(usuarioId),
                "/queue/conversas.nova",
                Map.of("conversaId", conversaId, "timestamp", System.currentTimeMillis())
            );
        } catch (Exception e) {
            System.err.println("Erro ao notificar nova conversa: " + e.getMessage());
        }
    }

    /**
     * Método utilitário para verificar se usuário está online
     */
    public boolean isUsuarioOnline(String nomeUsuario) {
        return usuariosOnline.containsKey(nomeUsuario);
    }

    /**
     * Método utilitário para obter contagem de usuários online
     */
    public int getContadorUsuariosOnline() {
        return usuariosOnline.size();
    }
}