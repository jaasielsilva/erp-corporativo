package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.NotificacaoChatDTO;
import com.jaasielsilva.portalceo.model.Mensagem;
import com.jaasielsilva.portalceo.model.NotificacaoChat;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.NotificacaoChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificacaoChatService {

    @Autowired
    private NotificacaoChatRepository notificacaoChatRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Criar notificação para nova mensagem
     */
    public NotificacaoChat criarNotificacaoNovaMensagem(Usuario destinatario, Usuario remetente, Mensagem mensagem) {
        NotificacaoChat notificacao = NotificacaoChat.criarNotificacaoNovaMensagem( remetente, mensagem);
        
        notificacao = notificacaoChatRepository.save(notificacao);
        
        // Enviar notificação via WebSocket
        NotificacaoChatDTO notificacaoDTO = NotificacaoChatDTO.fromEntity(notificacao);
        messagingTemplate.convertAndSendToUser(
                destinatario.getId().toString(),
                "/queue/notificacoes-chat",
                notificacaoDTO
        );
        
        // Atualizar contador de notificações não lidas
        long totalNaoLidas = contarNotificacoesNaoLidas(destinatario.getId());
        messagingTemplate.convertAndSendToUser(
                destinatario.getId().toString(),
                "/queue/contador-notificacoes",
                totalNaoLidas
        );
        
        return notificacao;
    }

    /**
     * Buscar notificações não lidas de um usuário
     */
    public List<NotificacaoChatDTO> buscarNotificacoesNaoLidas(Long usuarioId) {
        List<NotificacaoChat> notificacoes = notificacaoChatRepository.findNotificacaoNaoLidas(usuarioId);
        
        return notificacoes.stream()
                .map(NotificacaoChatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Contar notificações não lidas de um usuário
     */
    public long contarNotificacoesNaoLidas(Long usuarioId) {
        return notificacaoChatRepository.countNotificacaoNaoLidas(usuarioId);
    }

    /**
     * Buscar todas as notificações de um usuário
     */
    public List<NotificacaoChatDTO> buscarNotificacoesDoUsuario(Long usuarioId) {
        List<NotificacaoChat> notificacoes = notificacaoChatRepository.findNotificacoesDoUsuario(usuarioId);
        
        return notificacoes.stream()
                .map(NotificacaoChatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Buscar notificações de um remetente específico
     */
    public List<NotificacaoChatDTO> buscarNotificacoesDeRemetente(Long usuarioId, Long remetenteId) {
        List<NotificacaoChat> notificacoes = notificacaoChatRepository
                .findNotificacoesDeRemetente(usuarioId, remetenteId);
        
        return notificacoes.stream()
                .map(NotificacaoChatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Buscar notificações não lidas de um remetente específico
     */
    public List<NotificacaoChatDTO> buscarNotificacoesNaoLidasDeRemetente(Long usuarioId, Long remetenteId) {
        List<NotificacaoChat> notificacoes = notificacaoChatRepository
                .findNotificacoesNaoLidasDeRemetente(usuarioId, remetenteId);
        
        return notificacoes.stream()
                .map(NotificacaoChatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Marcar notificação como lida
     */
    public void marcarComoLida(Long notificacaoId) {
        NotificacaoChat notificacao = notificacaoChatRepository.findById(notificacaoId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));
        
        notificacao.marcarComoLida();
        notificacaoChatRepository.save(notificacao);
        
        // Atualizar contador via WebSocket
        long totalNaoLidas = contarNotificacoesNaoLidas(notificacao.getUsuario().getId());
        messagingTemplate.convertAndSendToUser(
                notificacao.getUsuario().getId().toString(),
                "/queue/contador-notificacoes",
                totalNaoLidas
        );
    }

    /**
     * Marcar todas as notificações de um usuário como lidas
     */
    public void marcarTodasComoLidas(Long usuarioId) {
        notificacaoChatRepository.marcarTodasComoLidas(usuarioId, LocalDateTime.now());
        
        // Atualizar contador via WebSocket
        messagingTemplate.convertAndSendToUser(
                usuarioId.toString(),
                "/queue/contador-notificacoes",
                0L
        );
    }

    /**
     * Marcar notificações de um remetente específico como lidas
     */
    public void marcarNotificacoesDeRemetenteComoLidas(Long usuarioId, Long remetenteId) {
        notificacaoChatRepository.marcarNotificacoesDeRemetenteComoLidas(
                usuarioId, remetenteId, LocalDateTime.now()
        );
        
        // Atualizar contador via WebSocket
        long totalNaoLidas = contarNotificacoesNaoLidas(usuarioId);
        messagingTemplate.convertAndSendToUser(
                usuarioId.toString(),
                "/queue/contador-notificacoes",
                totalNaoLidas
        );
    }

    /**
     * Buscar remetentes com notificações não lidas
     */
    public List<Usuario> buscarRemetentesComNotificacoesNaoLidas(Long usuarioId) {
        return notificacaoChatRepository.findRemetentesComNotificacoesNaoLidas(usuarioId);
    }

    /**
     * Contar notificações não lidas por remetente
     */
    public long contarNotificacoesNaoLidasDeRemetente(Long usuarioId, Long remetenteId) {
        return notificacaoChatRepository.countNotificacoesNaoLidasDeRemetente(usuarioId, remetenteId);
    }

    /**
     * Buscar notificações por tipo
     */
    public List<NotificacaoChatDTO> buscarNotificacoesPorTipo(Long usuarioId, NotificacaoChat.TipoNotificacao tipo) {
        List<NotificacaoChat> notificacoes = notificacaoChatRepository
                .findNotificacoesPorTipo(usuarioId, tipo);
        
        return notificacoes.stream()
                .map(NotificacaoChatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Buscar notificações por prioridade
     */
    public List<NotificacaoChatDTO> buscarNotificacoesPorPrioridade(Long usuarioId, NotificacaoChat.Prioridade prioridade) {
        List<NotificacaoChat> notificacoes = notificacaoChatRepository
                .findNotificacoesPorPrioridade(usuarioId, prioridade);
        
        return notificacoes.stream()
                .map(NotificacaoChatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Buscar notificações recentes (últimas 24 horas)
     */
    public List<NotificacaoChatDTO> buscarNotificacoesRecentes(Long usuarioId) {
        LocalDateTime dataInicio = LocalDateTime.now().minusHours(24);
        List<NotificacaoChat> notificacoes = notificacaoChatRepository
                .findNotificacoesRecentes(usuarioId, dataInicio);
        
        return notificacoes.stream()
                .map(NotificacaoChatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Limpar notificações antigas (mais de 30 dias)
     */
    @Transactional
    public void limparNotificacoesAntigas() {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(30);
        notificacaoChatRepository.deletarNotificacoesAntigas(dataLimite);
    }
}