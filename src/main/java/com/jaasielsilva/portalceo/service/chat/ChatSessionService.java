package com.jaasielsilva.portalceo.service.chat;

import com.jaasielsilva.portalceo.model.chat.ChatPresence;
import com.jaasielsilva.portalceo.repository.chat.ChatPresenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatSessionService {
    @Autowired
    private ChatPresenceRepository chatPresenceRepository;

    public void markOnline(Long userId) {
        ChatPresence p = chatPresenceRepository.findById(userId).orElseGet(() -> { ChatPresence c = new ChatPresence(); c.setUserId(userId); return c; });
        p.setStatus("ONLINE");
        p.setLastHeartbeat(LocalDateTime.now());
        chatPresenceRepository.save(p);
    }

    public void markOffline(Long userId) {
        ChatPresence p = chatPresenceRepository.findById(userId).orElseGet(() -> { ChatPresence c = new ChatPresence(); c.setUserId(userId); return c; });
        p.setStatus("OFFLINE");
        p.setLastHeartbeat(LocalDateTime.now());
        chatPresenceRepository.save(p);
    }

    public boolean isUserOnline(Long userId) {
        return chatPresenceRepository.findById(userId)
                .map(p -> "ONLINE".equals(p.getStatus()))
                .orElse(false);
    }
}
