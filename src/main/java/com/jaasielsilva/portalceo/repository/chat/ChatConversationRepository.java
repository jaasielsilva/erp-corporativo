package com.jaasielsilva.portalceo.repository.chat;

import com.jaasielsilva.portalceo.model.chat.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
}
