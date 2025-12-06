package com.jaasielsilva.portalceo.repository.chat;

import com.jaasielsilva.portalceo.model.chat.ChatConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatConversationParticipantRepository extends JpaRepository<ChatConversationParticipant, Long> {
    List<ChatConversationParticipant> findByConversationId(Long conversationId);
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}
