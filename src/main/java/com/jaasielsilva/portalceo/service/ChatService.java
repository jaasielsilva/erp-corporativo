package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.*;
import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private NotificacaoChatRepository notificacaoChatRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificacaoChatService notificacaoChatService;



    /**
     * Enviar uma nova mensagem
     */
    public MensagemDTO enviarMensagem(Long remetenteId, Long destinatarioId, String conteudo, Mensagem.TipoMensagem tipo) {
        Usuario remetente = usuarioRepository.findById(remetenteId)
                .orElseThrow(() -> new RuntimeException("Remetente não encontrado"));
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new RuntimeException("Destinatário não encontrado"));

        // Buscar ou criar conversa
        Conversa conversa = buscarOuCriarConversa(remetente, destinatario);

        // Criar mensagem
        Mensagem mensagem = new Mensagem();
        mensagem.setRemetente(remetente);
        mensagem.setDestinatario(destinatario);
        mensagem.setConteudo(conteudo);
        mensagem.setDataEnvio(LocalDateTime.now());
        mensagem.setLida(false);
        mensagem.setConversa(conversa);

        mensagem = mensagemRepository.save(mensagem);

        // Atualizar última atividade da conversa
        conversa.setUltimaAtividade(LocalDateTime.now());
        conversaRepository.save(conversa);

        // Criar notificação de chat
        notificacaoChatService.criarNotificacaoNovaMensagem(destinatario, remetente, mensagem);

        // Enviar mensagem via WebSocket
        MensagemDTO mensagemDTO = MensagemDTO.fromEntity(mensagem);
        messagingTemplate.convertAndSendToUser(
                destinatario.getId().toString(),
                "/queue/mensagens",
                mensagemDTO
        );

        return mensagemDTO;
    }

    /**
     * Buscar ou criar conversa entre dois usuários
     */
    private Conversa buscarOuCriarConversa(Usuario usuario1, Usuario usuario2) {
        Optional<Conversa> conversaExistente = conversaRepository
                .findConversaEntreUsuarios(usuario1.getId(), usuario2.getId());

        if (conversaExistente.isPresent()) {
            return conversaExistente.get();
        }

        // Criar nova conversa
        Conversa novaConversa = new Conversa();
        novaConversa.setUsuario1(usuario1);
        novaConversa.setUsuario2(usuario2);
        novaConversa.setDataCriacao(LocalDateTime.now());
        novaConversa.setUltimaAtividade(LocalDateTime.now());
        novaConversa.setAtiva(true);

        return conversaRepository.save(novaConversa);
    }

    /**
     * Buscar conversas de um usuário
     */
    public List<ConversaDTO> buscarConversasDoUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Conversa> conversas = conversaRepository.findConversasDoUsuario(usuarioId);

        return conversas.stream()
                .map(conversa -> ConversaDTO.fromEntity(conversa, usuario))
                .collect(Collectors.toList());
    }

    /**
     * Buscar mensagens de uma conversa com paginação
     */
    public List<MensagemDTO> buscarMensagensConversa(Long conversaId, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho);
        List<Mensagem> mensagens = mensagemRepository.findMensagensPorConversa(conversaId, pageable);

        return mensagens.stream()
                .map(MensagemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Buscar mensagens entre dois usuários
     */
    public List<MensagemDTO> buscarMensagensEntreUsuarios(Long usuario1Id, Long usuario2Id, int limite) {
        Pageable pageable = PageRequest.of(0, limite);
        List<Mensagem> mensagens = mensagemRepository.findMensagensEntreUsuarios(usuario1Id, usuario2Id, pageable);

        return mensagens.stream()
                .map(MensagemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Marcar mensagens como lidas
     */
    public void marcarMensagensComoLidas(Long usuarioId, Long remetenteId) {
        mensagemRepository.marcarMensagensComoLidas(usuarioId, remetenteId, LocalDateTime.now());
        
        // Marcar notificações como lidas
        notificacaoChatService.marcarNotificacoesDeRemetenteComoLidas(usuarioId, remetenteId);

        // Notificar via WebSocket sobre atualização de mensagens lidas
        messagingTemplate.convertAndSendToUser(
                remetenteId.toString(),
                "/queue/mensagens-lidas",
                usuarioId
        );
    }

    

    /**
     * Contar mensagens não lidas de um usuário
     */
    public long contarMensagensNaoLidas(Long usuarioId) {
        return mensagemRepository.countMensagensNaoLidas(usuarioId);
    }

    /**
     * Buscar remetentes com mensagens não lidas
     */
    public List<Usuario> buscarRemetentesComMensagensNaoLidas(Long usuarioId) {
        return mensagemRepository.findRemetentesComMensagensNaoLidas(usuarioId);
    }

    /**
     * Buscar conversa entre dois usuários
     */
    public Optional<ConversaDTO> buscarConversaEntreUsuarios(Long usuario1Id, Long usuario2Id) {
        Usuario usuario1 = usuarioRepository.findById(usuario1Id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Optional<Conversa> conversa = conversaRepository.findConversaEntreUsuarios(usuario1Id, usuario2Id);
        
        return conversa.map(c -> ConversaDTO.fromEntity(c, usuario1));
    }

    /**
     * Arquivar/desarquivar conversa
     */
    public void alterarStatusConversa(Long conversaId, boolean ativa) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada"));
        
        conversa.setAtiva(ativa);
        conversaRepository.save(conversa);
    }

    /**
     * Buscar usuários para iniciar conversa (excluindo o próprio usuário)
     */
    public List<Usuario> buscarUsuariosParaChat(Long usuarioLogadoId) {
        return usuarioRepository.findByIdNotAndStatusOrderByNome(usuarioLogadoId, Usuario.Status.ATIVO);
    }

    /**
     * Buscar usuários para iniciar conversa com filtro de busca
     */
    public List<Usuario> buscarUsuariosParaChat(Long usuarioLogadoId, String busca) {
        if (busca == null || busca.trim().isEmpty()) {
            return buscarUsuariosParaChat(usuarioLogadoId);
        }
        return usuarioRepository.findByIdNotAndStatusAndNomeContainingIgnoreCaseOrderByNome(
                usuarioLogadoId, Usuario.Status.ATIVO, busca.trim());
    }

    /**
     * Buscar mensagens de uma conversa com paginação (Page)
     */
    public Page<MensagemDTO> buscarMensagensDaConversa(Long conversaId, Long usuarioId, Pageable pageable) {
        // Verificar se o usuário tem acesso à conversa
        Optional<Conversa> conversa = conversaRepository.findById(conversaId);
        if (conversa.isEmpty()) {
            throw new RuntimeException("Conversa não encontrada");
        }
        
        Conversa conv = conversa.get();
        if (!conv.getUsuario1().getId().equals(usuarioId) && !conv.getUsuario2().getId().equals(usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta conversa");
        }
        
        List<Mensagem> mensagens = mensagemRepository.findMensagensPorConversa(conversaId, pageable);
        List<MensagemDTO> mensagensDTO = mensagens.stream()
                .map(MensagemDTO::fromEntity)
                .collect(Collectors.toList());
        
        long total = mensagemRepository.countMensagensPorConversa(conversaId);
        return new PageImpl<>(mensagensDTO, pageable, total);
    }

    /**
     * Buscar ou criar conversa entre dois usuários (retorna DTO)
     */
    public ConversaDTO buscarOuCriarConversa(Long usuario1Id, Long usuario2Id) {
        Usuario usuario1 = usuarioRepository.findById(usuario1Id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Usuario usuario2 = usuarioRepository.findById(usuario2Id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Conversa conversa = buscarOuCriarConversa(usuario1, usuario2);
        return ConversaDTO.fromEntity(conversa, usuario1);
    }

    /**
     * Enviar evento de digitação
     */
    public void enviarEventoDigitacao(Long usuarioId, Long destinatarioId, boolean digitando) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Optional<Conversa> conversa = conversaRepository.findConversaEntreUsuarios(usuarioId, destinatarioId);
        
        if (conversa.isPresent()) {
            DigitandoEventoDTO evento = digitando 
                ? DigitandoEventoDTO.criarEventoDigitando(usuarioId, usuario.getNome(), conversa.get().getId())
                : DigitandoEventoDTO.criarEventoParouDeDigitar(usuarioId, usuario.getNome(), conversa.get().getId());

            messagingTemplate.convertAndSendToUser(
                    destinatarioId.toString(),
                    "/queue/digitando",
                    evento
            );
        }
    }

    /**
     * Buscar última mensagem entre dois usuários
     */
    public Optional<MensagemDTO> buscarUltimaMensagem(Long usuario1Id, Long usuario2Id) {
        Optional<Mensagem> ultimaMensagem = mensagemRepository.findUltimaMensagemEntreUsuarios(usuario1Id, usuario2Id);
        return ultimaMensagem.map(MensagemDTO::fromEntity);
    }
}
