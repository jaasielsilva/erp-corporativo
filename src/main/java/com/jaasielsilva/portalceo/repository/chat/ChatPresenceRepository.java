package com.jaasielsilva.portalceo.repository.chat;

import com.jaasielsilva.portalceo.model.chat.ChatPresence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatPresenceRepository extends JpaRepository<ChatPresence, Long> {
}
