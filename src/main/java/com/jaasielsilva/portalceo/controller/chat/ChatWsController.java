package com.jaasielsilva.portalceo.controller.chat;

import com.jaasielsilva.portalceo.model.chat.ChatMessage;
import com.jaasielsilva.portalceo.service.chat.ChatMessageService;
import com.jaasielsilva.portalceo.service.chat.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWsController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private ChatSessionService chatSessionService;

    @MessageMapping("/presence/join")
    public void join(Authentication auth) {
        Long userId = auth != null && auth.getPrincipal() != null ? extractUserId(auth.getPrincipal()) : null;
        if (userId != null) chatSessionService.markOnline(userId);
        messagingTemplate.convertAndSend("/topic/presence", "ONLINE");
    }

    @MessageMapping("/presence/leave")
    public void leave(Authentication auth) {
        Long userId = auth != null && auth.getPrincipal() != null ? extractUserId(auth.getPrincipal()) : null;
        if (userId != null) chatSessionService.markOffline(userId);
        messagingTemplate.convertAndSend("/topic/presence", "OFFLINE");
    }

    @MessageMapping("/chat/send")
    public void send(@Payload ChatMessage payload, Authentication auth) {
        ChatMessage saved = chatMessageService.save(payload);
        messagingTemplate.convertAndSend("/topic/chat/" + saved.getConversationId(), saved);
    }

    private Long extractUserId(Object principal) {
        try { return (Long) principal.getClass().getMethod("getId").invoke(principal); } catch (Exception e) { return null; }
    }
}
