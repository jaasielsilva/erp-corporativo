package com.jaasielsilva.portalceo.service.chat;

import com.jaasielsilva.portalceo.model.chat.ChatMessage;
import com.jaasielsilva.portalceo.repository.chat.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage save(ChatMessage m) { return chatMessageRepository.save(m); }

    public Page<ChatMessage> history(Long conversationId, int page, int size) {
        return chatMessageRepository.findByConversationIdOrderBySentAtDesc(conversationId, PageRequest.of(page, size));
    }
}
