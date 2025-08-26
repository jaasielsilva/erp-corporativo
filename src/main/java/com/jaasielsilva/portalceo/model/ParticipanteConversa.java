package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa a participação de um usuário em uma conversa
 */
@Entity
@Table(name = "participantes_conversa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipanteConversa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;
    
    @Column(name = "adicionado_em", nullable = false)
    private LocalDateTime adicionadoEm;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "ultima_visualizacao")
    private LocalDateTime ultimaVisualizacao;
    
    @Column(name = "notificacoes_ativas", nullable = false)
    @Builder.Default
    private Boolean notificacoesAtivas = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoParticipante tipo = TipoParticipante.MEMBRO;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
    
    @Column(name = "removido_em")
    private LocalDateTime removidoEm;
    
    // Enum para tipos de participante
    public enum TipoParticipante {
        CRIADOR("Criador"),
        ADMINISTRADOR("Administrador"),
        MEMBRO("Membro");
        
        private final String descricao;
        
        TipoParticipante(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    // Métodos de conveniência
    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }
    
    public boolean isAdministrador() {
        return TipoParticipante.ADMINISTRADOR.equals(this.tipo) || 
               TipoParticipante.CRIADOR.equals(this.tipo);
    }
    
    public boolean isCriador() {
        return TipoParticipante.CRIADOR.equals(this.tipo);
    }
    
    public boolean temNotificacoesAtivas() {
        return Boolean.TRUE.equals(notificacoesAtivas);
    }
    
    public void atualizarUltimaVisualizacao() {
        this.ultimaVisualizacao = LocalDateTime.now();
    }
    
    public void remover() {
        this.ativo = false;
        this.removidoEm = LocalDateTime.now();
    }
    
    public void reativar() {
        this.ativo = true;
        this.removidoEm = null;
    }
    
    public void promoverAAdministrador() {
        if (!isCriador()) {
            this.tipo = TipoParticipante.ADMINISTRADOR;
        }
    }
    
    public void rebaixarAMembro() {
        if (!isCriador()) {
            this.tipo = TipoParticipante.MEMBRO;
        }
    }
    
    public void alterarNotificacoes(boolean ativas) {
        this.notificacoesAtivas = ativas;
    }
    
    @PrePersist
    protected void onCreate() {
        if (adicionadoEm == null) {
            adicionadoEm = LocalDateTime.now();
        }
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipanteConversa)) return false;
        ParticipanteConversa that = (ParticipanteConversa) o;
        return usuario != null && conversa != null && 
               usuario.getId().equals(that.usuario.getId()) && 
               conversa.getId().equals(that.conversa.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}