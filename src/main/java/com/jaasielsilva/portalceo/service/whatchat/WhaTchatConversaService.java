package com.jaasielsilva.portalceo.service.whatchat;

import com.jaasielsilva.portalceo.model.whatchat.ChatConversa;
import com.jaasielsilva.portalceo.model.whatchat.ChatConversaStatus;
import com.jaasielsilva.portalceo.repository.whatchat.ChatConversaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WhaTchatConversaService {

    private final ChatConversaRepository conversaRepository;

    @Transactional
    public ChatConversa obterOuCriar(String waId, String nomeContato) {
        String wa = PhoneNormalizer.digitsOnly(waId);
        if (wa == null) {
            throw new IllegalArgumentException("waId inválido");
        }

        ChatConversa conversa = conversaRepository.findByWaId(wa).orElseGet(() -> {
            ChatConversa c = new ChatConversa();
            c.setWaId(wa);
            c.setStatus(ChatConversaStatus.ABERTA);
            c.setUltimaMensagemEm(LocalDateTime.now());
            return c;
        });

        if (nomeContato != null && !nomeContato.isBlank()) {
            conversa.setNomeContato(nomeContato.trim());
        }
        if (conversa.getUltimaMensagemEm() == null) {
            conversa.setUltimaMensagemEm(LocalDateTime.now());
        }

        return conversaRepository.save(conversa);
    }

    @Transactional(readOnly = true)
    public List<ChatConversa> listar() {
        return conversaRepository.findAllByOrderByUltimaMensagemEmDescIdDesc();
    }

    @Transactional(readOnly = true)
    public ChatConversa buscarPorId(Long id) {
        return conversaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversa não encontrada"));
    }

    @Transactional
    public ChatConversa atualizarStatus(Long conversaId, ChatConversaStatus status) {
        ChatConversa c = buscarPorId(conversaId);
        c.setStatus(status);
        return conversaRepository.save(c);
    }

    @Transactional
    public ChatConversa marcarUltimaMensagem(ChatConversa conversa, LocalDateTime data) {
        conversa.setUltimaMensagemEm(data != null ? data : LocalDateTime.now());
        return conversaRepository.save(conversa);
    }
}

