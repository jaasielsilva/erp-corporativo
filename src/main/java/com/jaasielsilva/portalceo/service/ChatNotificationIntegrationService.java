package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.NotificacaoChat;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Mensagem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Serviço para integrar notificações de chat com o sistema principal de notificações
 */
@Service
@Transactional
public class ChatNotificationIntegrationService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificacaoChatService notificacaoChatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Cria notificação de nova mensagem no sistema principal
     */
    public void criarNotificacaoNovaMensagem(Usuario destinatario, Usuario remetente, Mensagem mensagem) {
        // Criar notificação no sistema de chat
        notificacaoChatService.criarNotificacaoNovaMensagem(destinatario, remetente, mensagem);
        
        // Criar notificação no sistema principal
        String titulo = "Nova mensagem de " + remetente.getNome();
        String conteudo = mensagem.getConteudo().length() > 100 ? 
                         mensagem.getConteudo().substring(0, 100) + "..." : 
                         mensagem.getConteudo();
        
        Notification notification = notificationService.createEntityNotification(
            "chat_message",
            titulo,
            conteudo,
            Notification.Priority.MEDIUM,
            destinatario,
            "mensagem",
            mensagem.getId()
        );
        
        // Enviar notificação via WebSocket para atualizar o badge
        enviarNotificacaoWebSocket(destinatario.getId(), titulo, conteudo);
    }

    /**
     * Marca notificações de chat como lidas no sistema principal
     */
    public void marcarNotificacoesChatComoLidas(Long usuarioId, Long remetenteId) {
        // Marcar no sistema de chat
        notificacaoChatService.marcarNotificacoesDeRemetenteComoLidas(usuarioId, remetenteId);
        
        // Enviar atualização via WebSocket
        messagingTemplate.convertAndSendToUser(
            usuarioId.toString(),
            "/queue/notificacoes-atualizadas",
            Map.of("tipo", "chat_lidas", "remetenteId", remetenteId)
        );
    }

    /**
     * Conta total de notificações não lidas (chat + sistema)
     */
    public long contarTotalNotificacoesNaoLidas(Usuario usuario) {
        long notificacoesChat = notificacaoChatService.contarNotificacoesNaoLidas(usuario.getId());
        long notificacoesSistema = notificationService.countUnreadNotificationsForUser(usuario);
        return notificacoesChat + notificacoesSistema;
    }

    /**
     * Envia notificação via WebSocket
     */
    private void enviarNotificacaoWebSocket(Long usuarioId, String titulo, String conteudo) {
        try {
            Map<String, Object> notificacao = Map.of(
                "tipo", "nova_mensagem",
                "titulo", titulo,
                "conteudo", conteudo,
                "timestamp", System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSendToUser(
                usuarioId.toString(),
                "/queue/notificacoes",
                notificacao
            );
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação WebSocket: " + e.getMessage());
        }
    }

    /**
     * Cria notificação de usuário online
     */
    public void notificarUsuarioOnline(Usuario usuario, Usuario usuarioOnline) {
        String titulo = usuarioOnline.getNome() + " está online";
        String conteudo = "Agora você pode conversar com " + usuarioOnline.getNome();
        
        notificationService.createNotification(
            "user_online",
            titulo,
            conteudo,
            Notification.Priority.LOW,
            usuario
        );
        
        enviarNotificacaoWebSocket(usuario.getId(), titulo, conteudo);
    }

    /**
     * Cria notificação de nova conversa iniciada
     */
    public void notificarNovaConversa(Usuario destinatario, Usuario remetente) {
        String titulo = remetente.getNome() + " iniciou uma conversa";
        String conteudo = "Você tem uma nova conversa com " + remetente.getNome();
        
        notificationService.createNotification(
            "new_conversation",
            titulo,
            conteudo,
            Notification.Priority.MEDIUM,
            destinatario
        );
        
        enviarNotificacaoWebSocket(destinatario.getId(), titulo, conteudo);
    }
}