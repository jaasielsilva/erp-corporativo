package com.jaasielsilva.portalceo.service.chat;

import com.jaasielsilva.portalceo.model.chat.ChatMessage;
import com.jaasielsilva.portalceo.repository.chat.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ChatMessageService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage save(ChatMessage m) {
        // Fallback para manter compatibilidade com banco se colunas forem NOT NULL
        if (m.getCiphertext() == null) {
            if (m.getContent() != null) {
                m.setCiphertext(m.getContent().getBytes(StandardCharsets.UTF_8));
            } else {
                m.setCiphertext(new byte[0]); // Fallback seguro para não quebrar insert
            }
        }
        if (m.getIv() == null) {
            m.setIv(new byte[12]);
        }
        return chatMessageRepository.save(m);
    }

    public Page<ChatMessage> history(Long conversationId, int page, int size) {
        return chatMessageRepository.findByConversationIdOrderBySentAtDesc(conversationId, PageRequest.of(page, size));
    }
}
