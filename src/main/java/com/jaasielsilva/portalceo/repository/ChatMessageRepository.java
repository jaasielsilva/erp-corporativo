package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Mensagens públicas ou privadas para um usuário específico
    List<ChatMessage> findByDestinatarioIdOrDestinatarioIdIsNullOrderByTimestampAsc(Long destinatarioId);
}
