package com.jaasielsilva.portalceo.repository.chat;

import com.jaasielsilva.portalceo.model.chat.ChatMembership;
import com.jaasielsilva.portalceo.model.chat.ChatRoom;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatMembershipRepository extends JpaRepository<ChatMembership, Long> {
    List<ChatMembership> findByRoomIdAndActiveTrue(Long roomId);
    Optional<ChatMembership> findByRoomAndUsuario(ChatRoom room, Usuario usuario);
    List<ChatMembership> findByUsuarioIdAndActiveTrue(Long usuarioId);
}