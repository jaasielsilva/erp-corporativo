package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.ChatMessage;
import com.jaasielsilva.portalceo.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage salvarMensagem(String sender, String content, Long destinatarioId) {
        ChatMessage msg = new ChatMessage(sender, content, LocalDateTime.now(), destinatarioId);
        return chatMessageRepository.save(msg);
    }

    public List<ChatMessage> getMensagensParaUsuario(Long usuarioId) {
        return chatMessageRepository.findByDestinatarioIdOrDestinatarioIdIsNullOrderByTimestampAsc(usuarioId);
    }

    public List<ChatMessage> getTodasMensagens() {
        return chatMessageRepository.findAll();
    }
}
