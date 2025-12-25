package com.jaasielsilva.portalceo.repository.whatchat;

import com.jaasielsilva.portalceo.model.whatchat.ChatMensagem;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagemDirecao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMensagemRepository extends JpaRepository<ChatMensagem, Long> {
    List<ChatMensagem> findByConversa_IdOrderByDataMensagemAscIdAsc(Long conversaId);
    Optional<ChatMensagem> findByWhatsappMessageId(String whatsappMessageId);
    boolean existsByConversa_IdAndDirecao(Long conversaId, ChatMensagemDirecao direcao);
}
