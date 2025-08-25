package com.jaasielsilva.portalceo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;

@Service
public class NotificacaoService {

    private final WorkflowAdesaoService workflowAdesaoService;

    public NotificacaoService(@Lazy WorkflowAdesaoService workflowAdesaoService) {
        this.workflowAdesaoService = workflowAdesaoService;
    }

    // Armazenamento em memória das notificações (em produção, usar banco de dados)
    private final ConcurrentHashMap<String, List<Notificacao>> notificacoesPorUsuario = new ConcurrentHashMap<>();

    /**
     * Classe interna para representar uma notificação
     */
    public static class Notificacao {
        private String id;
        private String titulo;
        private String mensagem;
        private String tipo; // info, warning, success, error
        private String url;
        private LocalDateTime dataHora;
        private boolean lida;

        public Notificacao(String id, String titulo, String mensagem, String tipo, String url) {
            this.id = id;
            this.titulo = titulo;
            this.mensagem = mensagem;
            this.tipo = tipo;
            this.url = url;
            this.dataHora = LocalDateTime.now();
            this.lida = false;
        }

        // Getters e Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public LocalDateTime getDataHora() { return dataHora; }
        public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
        public boolean isLida() { return lida; }
        public void setLida(boolean lida) { this.lida = lida; }
    }

    /**
     * Adiciona uma notificação para um usuário específico
     */
    public void adicionarNotificacao(String emailUsuario, String titulo, String mensagem, String tipo, String url) {
        String notificacaoId = System.currentTimeMillis() + "_" + emailUsuario.hashCode();
        Notificacao notificacao = new Notificacao(notificacaoId, titulo, mensagem, tipo, url);

        notificacoesPorUsuario.computeIfAbsent(emailUsuario, k -> new CopyOnWriteArrayList<>()).add(notificacao);

        // Manter apenas as últimas 50 notificações por usuário
        List<Notificacao> notificacoes = notificacoesPorUsuario.get(emailUsuario);
        if (notificacoes.size() > 50) {
            notificacoes.remove(0);
        }
    }

    public List<Notificacao> obterNotificacoes(String emailUsuario) {
        return new ArrayList<>(notificacoesPorUsuario.getOrDefault(emailUsuario, new ArrayList<>()));
    }

    public List<Notificacao> obterNotificacaoesNaoLidas(String emailUsuario) {
        return notificacoesPorUsuario.getOrDefault(emailUsuario, new ArrayList<>())
                .stream()
                .filter(n -> !n.isLida())
                .toList();
    }

    public void marcarComoLida(String emailUsuario, String notificacaoId) {
        List<Notificacao> notificacoes = notificacoesPorUsuario.get(emailUsuario);
        if (notificacoes != null) {
            notificacoes.stream()
                    .filter(n -> n.getId().equals(notificacaoId))
                    .findFirst()
                    .ifPresent(n -> n.setLida(true));
        }
    }

    public void marcarTodasComoLidas(String emailUsuario) {
        List<Notificacao> notificacoes = notificacoesPorUsuario.get(emailUsuario);
        if (notificacoes != null) {
            notificacoes.forEach(n -> n.setLida(true));
        }
    }

    public long contarNotificacaoesNaoLidas(String emailUsuario) {
        return obterNotificacaoesNaoLidas(emailUsuario).size();
    }

    public void notificarNovoProcessoAdesao(ProcessoAdesao processo) {
        String titulo = "Novo Processo de Adesão";
        String mensagem = String.format("Processo de adesão de %s aguardando aprovação",
                processo.getNomeColaborador());
        String url = "/rh/workflow/processo/" + processo.getId();

        List<String> usuariosGerenciais = List.of(
                "admin@empresa.com",
                "rh@empresa.com",
                "gerente@empresa.com");

        usuariosGerenciais.forEach(email -> adicionarNotificacao(email, titulo, mensagem, "warning", url));
    }

    public void notificarProcessoAprovado(ProcessoAdesao processo) {
        String titulo = "Processo Aprovado";
        String mensagem = String.format("Processo de adesão de %s foi aprovado",
                processo.getNomeColaborador());
        String url = "/rh/colaboradores/adesao/status/" + processo.getSessionId();

        if (processo.getEmailColaborador() != null) {
            adicionarNotificacao(processo.getEmailColaborador(), titulo, mensagem, "success", url);
        }
    }

    public void notificarProcessoRejeitado(ProcessoAdesao processo) {
        String titulo = "Processo Rejeitado";
        String mensagem = String.format("Processo de adesão de %s foi rejeitado: %s",
                processo.getNomeColaborador(), processo.getMotivoRejeicao());
        String url = "/rh/colaboradores/adesao/status/" + processo.getSessionId();

        if (processo.getEmailColaborador() != null) {
            adicionarNotificacao(processo.getEmailColaborador(), titulo, mensagem, "error", url);
        }
    }

    public void limparNotificacaoesAntigas() {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);

        notificacoesPorUsuario.values()
                .forEach(notificacoes -> notificacoes.removeIf(n -> n.getDataHora().isBefore(limite)));
    }

    public void notificarNovoColaborador(Colaborador colaborador) {
        if (colaborador == null || colaborador.getEmail() == null) {
            System.err.println("Colaborador inválido para notificação");
            return;
        }

        String titulo = "Novo Colaborador Cadastrado";
        String mensagem = String.format("O colaborador %s (%s) foi cadastrado no sistema.",
                colaborador.getNome(), colaborador.getEmail());
        String url = "/rh/colaboradores/" + colaborador.getId();

        List<String> usuariosGerenciais = List.of(
                "admin@empresa.com",
                "rh@empresa.com",
                "gerente@empresa.com");

        usuariosGerenciais.forEach(email -> adicionarNotificacao(email, titulo, mensagem, "info", url));

        System.out.println("Notificações enviadas sobre novo colaborador: " + colaborador.getNome());
    }
}
