package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes_chat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;

    @Column(name = "mensagem_id", nullable = false)
    private Long mensagemId;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "lida", nullable = false)
    private boolean lida = false;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_leitura")
    private LocalDateTime dataLeitura;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoNotificacao tipo = TipoNotificacao.NOVA_MENSAGEM;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade")
    private Prioridade prioridade = Prioridade.NORMAL;

    public enum TipoNotificacao {
        NOVA_MENSAGEM,
        MENSAGEM_LIDA,
        USUARIO_ONLINE,
        USUARIO_DIGITANDO
    }

    public enum Prioridade {
        BAIXA,
        NORMAL,
        ALTA,
        URGENTE
    }

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    // Método para marcar como lida
    public void marcarComoLida() {
        this.lida = true;
        this.dataLeitura = LocalDateTime.now();
    }

    // Método estático para criar notificação de nova mensagem
    public static NotificacaoChat criarNotificacaoNovaMensagem(Usuario destinatario, com.jaasielsilva.portalceo.model.chat.ChatMessage mensagem) {
        return NotificacaoChat.builder()
                .usuario(destinatario)
                .remetente(mensagem.getSender())
                .mensagemId(mensagem.getId())
                .titulo("Nova mensagem de " + mensagem.getSender().getNome())
                .conteudo(mensagem.getContent().length() > 100 ? 
                         mensagem.getContent().substring(0, 100) + "..." : 
                         mensagem.getContent())
                .tipo(TipoNotificacao.NOVA_MENSAGEM)
                .prioridade(Prioridade.NORMAL)
                .build();
    }
}