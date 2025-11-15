package com.jaasielsilva.portalceo.repository.chat;

import com.jaasielsilva.portalceo.model.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("select m from ChatMessage m where m.room.id=:roomId order by m.sentAt asc")
    List<ChatMessage> findByRoomOrdered(@Param("roomId") Long roomId);
    @Query("select count(m) from ChatMessage m where m.room.id=:roomId and m.read=false and m.sender.id<>:userId")
    long countUnread(@Param("roomId") Long roomId, @Param("userId") Long userId);
}