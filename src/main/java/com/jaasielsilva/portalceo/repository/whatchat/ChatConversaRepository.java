package com.jaasielsilva.portalceo.repository.whatchat;

import com.jaasielsilva.portalceo.model.whatchat.ChatConversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversaRepository extends JpaRepository<ChatConversa, Long> {
    Optional<ChatConversa> findByWaId(String waId);
    List<ChatConversa> findAllByOrderByUltimaMensagemEmDescIdDesc();
}

