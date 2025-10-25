package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Mensagem;
import com.jaasielsilva.portalceo.model.ReacaoMensagem;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.MensagemRepository;
import com.jaasielsilva.portalceo.repository.ReacaoMensagemRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReacaoMensagemService {

    @Autowired
    private ReacaoMensagemRepository reacaoMensagemRepository;

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ChatService chatService;

    public ReacaoMensagem adicionarReacao(Mensagem mensagem, Usuario usuario, String emoji) {
        // Verificar se o usuário já reagiu com o mesmo emoji
        Optional<ReacaoMensagem> reacaoExistente = reacaoMensagemRepository.findByMensagemId(mensagem.getId()).stream()
                .filter(r -> r.getUsuario().getId().equals(usuario.getId()) && r.getEmoji().equals(emoji))
                .findFirst();

        if (reacaoExistente.isPresent()) {
            // Se já existe, remove a reação (toggle)
            ReacaoMensagem reacaoRemovida = reacaoExistente.get();
            reacaoMensagemRepository.delete(reacaoRemovida);
            chatService.notificarReacao(mensagem.getId(), mensagem.getConversa().getId(), null); // Notifica remoção
            return null; // Indica que a reação foi removida
        } else {
            // Se não existe, adiciona a nova reação
            ReacaoMensagem reacao = new ReacaoMensagem();
            reacao.setMensagem(mensagem);
            reacao.setUsuario(usuario);
            reacao.setEmoji(emoji);
            reacao.setDataReacao(LocalDateTime.now());
            ReacaoMensagem novaReacao = reacaoMensagemRepository.save(reacao);
            chatService.notificarReacao(mensagem.getId(), mensagem.getConversa().getId(), novaReacao); // Notifica adição
            return novaReacao;
        }
    }

    public void removerReacao(Long reacaoId) {
        reacaoMensagemRepository.deleteById(reacaoId);
    }

    public List<ReacaoMensagem> buscarReacoesPorMensagem(Long mensagemId) {
        return reacaoMensagemRepository.findByMensagemId(mensagemId);
    }
}