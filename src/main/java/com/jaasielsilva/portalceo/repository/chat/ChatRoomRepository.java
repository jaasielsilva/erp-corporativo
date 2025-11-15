package com.jaasielsilva.portalceo.repository.chat;

import com.jaasielsilva.portalceo.model.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("select r from ChatRoom r join ChatMembership m on m.room.id=r.id where m.usuario.id=:userId and m.active=true and r.active=true order by r.id desc")
    List<ChatRoom> findRoomsByUser(@Param("userId") Long userId);
}