package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa uma mensagem no sistema de chat interno
 */
@Entity
@Table(name = "mensagens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "enviada_em", nullable = false)
    private LocalDateTime enviadaEm;

    @Column(name = "lida", nullable = false)

    private Boolean lida = false;

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)

    private TipoMensagem tipo = TipoMensagem.TEXTO;

    @Column(name = "editada_em")
    private LocalDateTime editadaEm;

    @Column(name = "arquivo_url")
    private String arquivoUrl;

    @Column(name = "arquivo_nome")
    private String arquivoNome;

    @Column(name = "arquivo_tamanho")
    private Long arquivoTamanho;

    // Resposta a outra mensagem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resposta_a_id")
    private Mensagem respostaA;

    // Enum para tipos de mensagem
    public enum TipoMensagem {
        TEXTO("Texto"),
        ARQUIVO("Arquivo"),
        IMAGEM("Imagem"),
        SISTEMA("Sistema"),
        NOTIFICACAO("Notificação");

        private final String descricao;

        TipoMensagem(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Métodos de conveniência
    public boolean isLida() {
        return Boolean.TRUE.equals(lida);
    }

    public void marcarComoLida() {
        this.lida = true;
        this.lidaEm = LocalDateTime.now();
    }

    public boolean temArquivo() {
        return arquivoUrl != null && !arquivoUrl.trim().isEmpty();
    }

    public boolean isEditada() {
        return editadaEm != null;
    }

    public boolean isResposta() {
        return respostaA != null;
    }

    public boolean isSistema() {
        return TipoMensagem.SISTEMA.equals(this.tipo);
    }

    public String getConteudoResumo(int maxLength) {
        if (conteudo == null)
            return "";
        if (conteudo.length() <= maxLength)
            return conteudo;
        return conteudo.substring(0, maxLength) + "...";
    }

    @PrePersist
    protected void onCreate() {
        if (enviadaEm == null) {
            enviadaEm = LocalDateTime.now();
        }
    }

    // Método para verificar se a mensagem é do usuário
    public boolean isDoUsuario(Usuario usuario) {
        return this.remetente.getId().equals(usuario.getId());
    }

    // Método para obter outro participante em conversas individuais
    public Usuario getOutroParticipante(Usuario usuarioAtual) {
        return this.conversa.getParticipantes().stream()
                .map(ParticipanteConversa::getUsuario)
                .filter(u -> !u.getId().equals(usuarioAtual.getId()))
                .findFirst()
                .orElse(null);
    }

}