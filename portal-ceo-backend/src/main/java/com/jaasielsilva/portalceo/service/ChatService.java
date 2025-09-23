package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.ConversaDTO;
import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsável por toda a lógica de negócio do sistema de chat interno
 * Gerencia conversas, mensagens, participantes e notificações em tempo real
 */
@Service
@Transactional
public class ChatService {

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private ParticipanteConversaRepository participanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ==================== CONVERSAS ====================

    /**
     * Cria uma nova conversa individual entre dois usuários
     */
    public Conversa criarConversaIndividual(Long usuarioId1, Long usuarioId2) {
        // Verificar se já existe conversa entre os usuários
        Optional<Conversa> conversaExistente = conversaRepository
                .findConversaEntreUsuarios(usuarioId1, usuarioId2);

        if (conversaExistente.isPresent()) {
            return conversaExistente.get();
        }

        Usuario usuario1 = usuarioRepository.findById(usuarioId1)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + usuarioId1));
        Usuario usuario2 = usuarioRepository.findById(usuarioId2)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + usuarioId2));

        // Criar nova conversa individual usando método estático
        Conversa conversa = Conversa.criarConversaIndividual(usuario1.getId(), usuario2.getId(), usuario1.getId());
        conversa = conversaRepository.save(conversa);

        // Adicionar participantes
        adicionarParticipante(conversa.getId(), usuario1.getId(), ParticipanteConversa.TipoParticipante.MEMBRO);
        adicionarParticipante(conversa.getId(), usuario2.getId(), ParticipanteConversa.TipoParticipante.MEMBRO);

        return conversa;
    }

    /**
     * Cria uma nova conversa em grupo
     */
    public Conversa criarConversaGrupo(String titulo, Long criadoPorId, List<Long> participantesIds) {
        Usuario criador = usuarioRepository.findById(criadoPorId)
                .orElseThrow(() -> new RuntimeException("Usuário criador não encontrado: " + criadoPorId));

        // Criar nova conversa em grupo usando método estático
        Conversa conversa = Conversa.criarConversaGrupo(titulo, criador.getId());
        conversa = conversaRepository.save(conversa);

        // Adicionar criador como administrador
        adicionarParticipante(conversa.getId(), criador.getId(), ParticipanteConversa.TipoParticipante.CRIADOR);

        // Adicionar outros participantes como membros
        if (participantesIds != null) {
            for (Long participanteId : participantesIds) {
                if (!participanteId.equals(criadoPorId)) {
                    adicionarParticipante(conversa.getId(), participanteId,
                            ParticipanteConversa.TipoParticipante.MEMBRO);
                }
            }
        }

        // Enviar mensagem de sistema informando criação do grupo
        enviarMensagemSistema(conversa.getId(), "Grupo '" + titulo + "' foi criado por " + criador.getNome());

        return conversa;
    }

    /**
     * Busca conversas do usuário
     */
    @Transactional(readOnly = true)
    public List<Conversa> buscarConversasDoUsuario(Long usuarioId) {
        return conversaRepository.findConversasDoUsuario(usuarioId);
    }

    /**
     * Busca conversas do usuário retornando DTO com dados completos
     */
    @Transactional(readOnly = true)
    public List<ConversaDTO> buscarConversasDoUsuarioDTO(Long usuarioId, Usuario usuarioLogado) {
        List<Conversa> conversas = conversaRepository.findConversasDoUsuario(usuarioId);
        return conversas.stream()
                .map(conversa -> ConversaDTO.fromEntity(conversa, usuarioLogado))
                .collect(Collectors.toList());
    }

    /**
     * Busca conversas individuais do usuário
     */
    @Transactional(readOnly = true)
    public List<Conversa> buscarConversasIndividuaisDoUsuario(Long usuarioId) {
        return conversaRepository.findConversasIndividuaisDoUsuario(usuarioId);
    }

    /**
     * Busca conversas em grupo do usuário
     */
    @Transactional(readOnly = true)
    public List<Conversa> buscarConversasGrupoDoUsuario(Long usuarioId) {
        return conversaRepository.findConversasGrupoDoUsuario(usuarioId);
    }

    /**
     * Busca conversas por tipo
     */
    @Transactional(readOnly = true)
    public List<Conversa> buscarConversasPorTipo(Long usuarioId, Conversa.TipoConversa tipo) {
        return conversaRepository.findConversasPorTipo(usuarioId, tipo.name());
    }

    /**
     * Busca conversa por ID
     */
    @Transactional(readOnly = true)
    public Optional<Conversa> buscarConversaPorId(Long conversaId) {
        return conversaRepository.findById(conversaId);
    }

    // ==================== MENSAGENS ====================

    /**
     * Envia uma nova mensagem
     */
    public Mensagem enviarMensagem(Long conversaId, Long remetenteId, String conteudo) {
        return enviarMensagem(conversaId, remetenteId, conteudo, Mensagem.TipoMensagem.TEXTO);
    }

    /**
     * Envia uma nova mensagem com tipo específico
     */
    public Mensagem enviarMensagem(Long conversaId, Long remetenteId, String conteudo, Mensagem.TipoMensagem tipo) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada: " + conversaId));

        Usuario remetente = usuarioRepository.findById(remetenteId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + remetenteId));

        // Verificar se usuário é participante da conversa
        Optional<ParticipanteConversa> participante = participanteRepository
                .findParticipanteAtivoByUsuarioIdAndConversaId(remetenteId, conversaId);

        if (participante.isEmpty()) {
            throw new RuntimeException("Usuário não é participante desta conversa");
        }

        // Determinar destinatário baseado no tipo de conversa
        Usuario destinatario = null;
        if (conversa.getTipo() == Conversa.TipoConversa.INDIVIDUAL) {
            // Para conversa individual, o destinatário é o outro participante
            List<ParticipanteConversa> participantesAtivos = participanteRepository
                    .findParticipantesAtivosByConversaId(conversaId);
            for (ParticipanteConversa p : participantesAtivos) {
                if (!p.getUsuario().getId().equals(remetenteId)) {
                    destinatario = p.getUsuario();
                    break;
                }
            }
        } else {
            // Para conversas em grupo, usar o criador da conversa como destinatário padrão
            if (conversa.getCriadoPor() != null) {
                destinatario = usuarioRepository.findById(conversa.getCriadoPor())
                        .orElse(remetente); // fallback para o próprio remetente
            } else {
                destinatario = remetente; // fallback para o próprio remetente
            }
        }

        // Criar e salvar mensagem
        Mensagem mensagem = new Mensagem();
        mensagem.setConteudo(conteudo);
        mensagem.setRemetente(remetente);
        mensagem.setDestinatario(destinatario);
        mensagem.setConversa(conversa);
        mensagem.setTipo(tipo);
        mensagem.setDataEnvio(LocalDateTime.now());
        mensagem.setLida(false);

        mensagem = mensagemRepository.save(mensagem);

        // Atualizar última atividade da conversa
        conversa.setUltimaAtividade(LocalDateTime.now());
        conversaRepository.save(conversa);

        // Enviar notificação em tempo real para todos os participantes
        notificarParticipantes(conversa, mensagem);

        return mensagem;
    }

    /**
     * Envia mensagem de sistema
     */
    public Mensagem enviarMensagemSistema(Long conversaId, String conteudo) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada: " + conversaId));

        // Para mensagens de sistema, usar o criador da conversa como remetente
        Usuario remetente;
        if (conversa.getCriadoPor() != null) {
            remetente = usuarioRepository.findById(conversa.getCriadoPor())
                    .orElseThrow(() -> new RuntimeException("Usuário criador não encontrado"));
        } else if (conversa.getUsuario1Id() != null) {
            remetente = usuarioRepository.findById(conversa.getUsuario1Id())
                    .orElseThrow(() -> new RuntimeException("Usuário da conversa não encontrado"));
        } else {
            throw new RuntimeException("Não foi possível determinar remetente para mensagem de sistema");
        }

        // Para mensagens de sistema, usar o remetente como destinatário padrão
        Usuario destinatario = remetente;

        Mensagem mensagem = new Mensagem();
        mensagem.setConteudo(conteudo);
        mensagem.setRemetente(remetente);
        mensagem.setDestinatario(destinatario);
        mensagem.setConversa(conversa);
        mensagem.setTipo(Mensagem.TipoMensagem.SISTEMA);
        mensagem.setDataEnvio(LocalDateTime.now());
        mensagem.setLida(false);

        mensagem = mensagemRepository.save(mensagem);

        // Notificar participantes
        notificarParticipantes(conversa, mensagem);

        return mensagem;
    }

    /**
     * Busca mensagens de uma conversa
     */
    @Transactional(readOnly = true)
    public List<Mensagem> buscarMensagensConversa(Long conversaId) {
        return mensagemRepository.findByConversaIdOrderByDataEnvioAsc(conversaId);
    }

    /**
     * Marca mensagem como lida
     */
    public void marcarMensagensDaConversaComoLidas(Long conversaId, Long usuarioId) {
        List<Mensagem> mensagens = mensagemRepository.findByConversaIdOrderByDataEnvioAsc(conversaId);

        for (Mensagem mensagem : mensagens) {
            if (!mensagem.getRemetente().getId().equals(usuarioId) && !mensagem.isLida()) {
                mensagem.marcarComoLida();
                mensagemRepository.save(mensagem);
            }
        }

        // Atualizar última visualização do participante
        atualizarUltimaVisualizacao(conversaId, usuarioId);
    }

    // ==================== PARTICIPANTES ====================

    /**
     * Adiciona um participante à conversa
     */
    public ParticipanteConversa adicionarParticipante(Long conversaId, Long usuarioId,
            ParticipanteConversa.TipoParticipante tipo) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada: " + conversaId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + usuarioId));

        // Verificar se já é participante
        Optional<ParticipanteConversa> participanteExistente = participanteRepository
                .findByUsuarioIdAndConversaId(usuarioId, conversaId);

        if (participanteExistente.isPresent()) {
            ParticipanteConversa participante = participanteExistente.get();
            if (!participante.isAtivo()) {
                participante.reativar();
                return participanteRepository.save(participante);
            }
            return participante;
        }

        // Criar novo participante
        ParticipanteConversa participante = new ParticipanteConversa();
        participante.setUsuario(usuario);
        participante.setConversa(conversa);
        participante.setTipo(tipo);
        participante.setAdicionadoEm(LocalDateTime.now());
        participante.setAtivo(true);
        participante.setNotificacoesAtivas(true);

        participante = participanteRepository.save(participante);

        // Enviar mensagem de sistema
        if (!tipo.equals(ParticipanteConversa.TipoParticipante.CRIADOR)) {
            enviarMensagemSistema(conversaId,
                    usuario.getNome() + " foi adicionado ao grupo");
        }

        return participante;
    }

    /**
     * Remove um participante da conversa
     */
    public void removerParticipante(Long conversaId, Long usuarioId) {
        ParticipanteConversa participante = participanteRepository
                .findByUsuarioIdAndConversaId(usuarioId, conversaId)
                .orElseThrow(() -> new RuntimeException("Participante não encontrado"));

        participante.remover();
        participanteRepository.save(participante);

        // Enviar mensagem de sistema
        enviarMensagemSistema(conversaId,
                participante.getUsuario().getNome() + " foi removido do grupo");
    }

    /**
     * Busca participantes ativos de uma conversa
     */
    @Transactional(readOnly = true)
    public List<ParticipanteConversa> buscarParticipantesAtivos(Long conversaId) {
        return participanteRepository.findParticipantesAtivosByConversaId(conversaId);
    }

    // ==================== NOTIFICAÇÕES ====================

    /**
     * Notifica todos os participantes sobre nova mensagem
     */
    private void notificarParticipantes(Conversa conversa, Mensagem mensagem) {
        List<ParticipanteConversa> participantes = buscarParticipantesAtivos(conversa.getId());

        for (ParticipanteConversa participante : participantes) {
            // Não notificar o próprio remetente
            if (!participante.getUsuario().getId().equals(mensagem.getRemetente().getId())) {
                // Enviar notificação via WebSocket
                messagingTemplate.convertAndSendToUser(
                        participante.getUsuario().getEmail(),
                        "/queue/mensagens",
                        criarNotificacaoMensagem(mensagem));
            }
        }
    }

    /**
     * Cria objeto de notificação para envio via WebSocket
     */
    private Object criarNotificacaoMensagem(Mensagem mensagem) {
        return new Object() {
            public final Long conversaId = mensagem.getConversa().getId();
            public final Long mensagemId = mensagem.getId();
            public final String conteudo = mensagem.getConteudo();
            public final String remetente = mensagem.getRemetente().getNome();
            public final String tipo = mensagem.getTipo().name();
            public final String timestamp = mensagem.getDataEnvio().toString();
        };
    }

    // ==================== UTILITÁRIOS ====================

    /**
     * Conta mensagens não lidas do usuário
     */
    @Transactional(readOnly = true)
    public long contarMensagensNaoLidas(Long usuarioId) {
        return mensagemRepository.countMensagensNaoLidas(usuarioId);
    }

    /**
     * Atualiza última visualização do participante
     */
    public void atualizarUltimaVisualizacao(Long conversaId, Long usuarioId) {
        participanteRepository.atualizarUltimaVisualizacao(usuarioId, conversaId, LocalDateTime.now());
    }

    /**
     * Verifica se usuário pode gerenciar participantes (deve ser criador ou
     * administrador)
     */
    @Transactional(readOnly = true)
    public boolean podeGerenciarParticipantes(Long conversaId, Long usuarioId) {
        Optional<ParticipanteConversa> participante = participanteRepository
                .findParticipanteAtivoByUsuarioIdAndConversaId(usuarioId, conversaId);

        if (participante.isPresent()) {
            ParticipanteConversa.TipoParticipante tipo = participante.get().getTipo();
            return tipo == ParticipanteConversa.TipoParticipante.CRIADOR ||
                    tipo == ParticipanteConversa.TipoParticipante.ADMINISTRADOR;
        }

        return false;
    }

    /**
     * Verifica se usuário é participante da conversa
     */
    @Transactional(readOnly = true)
    public boolean isParticipante(Long conversaId, Long usuarioId) {
        return participanteRepository.findParticipanteAtivoByUsuarioIdAndConversaId(usuarioId, conversaId).isPresent();
    }

    /**
     * Atualiza status online do usuário por ID
     */
    public void atualizarStatusOnline(Long usuarioId, boolean online) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + usuarioId));

        // Atualizar status no banco de dados
        usuario.setOnline(online);
        usuarioRepository.save(usuario);

        // Notificar contatos sobre mudança de status
        final String tipoStatus = online ? "USUARIO_ONLINE" : "USUARIO_OFFLINE";
        messagingTemplate.convertAndSend("/topic/usuarios.status", new Object() {
            public final Long usuarioId = usuario.getId();
            public final String nome = usuario.getNome();
            public final String tipo = tipoStatus;
        });
    }

    /**
     * Atualiza status online do usuário por email
     */
    public void atualizarStatusOnline(String email, boolean online) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));

        // Atualizar status no banco de dados
        usuario.setOnline(online);
        usuarioRepository.save(usuario);

        // Notificar contatos sobre mudança de status
        final String tipoStatus = online ? "USUARIO_ONLINE" : "USUARIO_OFFLINE";
        messagingTemplate.convertAndSend("/topic/usuarios.status", new Object() {
            public final Long usuarioId = usuario.getId();
            public final String nome = usuario.getNome();
            public final String tipo = tipoStatus;
        });
    }
}