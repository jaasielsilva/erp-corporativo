package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Listener para eventos de WebSocket
 * Gerencia automaticamente o status online/offline dos usuários
 * 
 * @author Sistema ERP
 * @version 1.0
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private ChatService chatService;

    /**
     * Evento disparado quando um usuário conecta via WebSocket
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null) {
            System.out.println("🔗 Usuário conectado: " + user.getName());
            
            try {
                // Marcar usuário como online
                chatService.atualizarStatusOnline(user.getName(), true);
            } catch (Exception e) {
                System.err.println("Erro ao marcar usuário como online: " + e.getMessage());
            }
        }
    }

    /**
     * Evento disparado quando um usuário desconecta do WebSocket
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null) {
            System.out.println("🔌 Usuário desconectado: " + user.getName());
            
            try {
                // Marcar usuário como offline
                chatService.atualizarStatusOnline(user.getName(), false);
            } catch (Exception e) {
                System.err.println("Erro ao marcar usuário como offline: " + e.getMessage());
            }
        }
    }
}