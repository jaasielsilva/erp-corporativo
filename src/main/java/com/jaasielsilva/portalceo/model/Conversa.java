package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario1_id", nullable = false)
    private Usuario usuario1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario2_id", nullable = false)
    private Usuario usuario2;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "ultima_atividade")
    private LocalDateTime ultimaAtividade;

    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    @OneToMany(mappedBy = "conversa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dataEnvio DESC")
    private List<Mensagem> mensagens;

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (ultimaAtividade == null) {
            ultimaAtividade = LocalDateTime.now();
        }
    }

    // Método para verificar se o usuário participa da conversa
    public boolean participaUsuario(Usuario usuario) {
        return this.usuario1.getId().equals(usuario.getId()) || 
               this.usuario2.getId().equals(usuario.getId());
    }

    // Método para obter o outro participante da conversa
    public Usuario getOutroParticipante(Usuario usuarioAtual) {
        return this.usuario1.getId().equals(usuarioAtual.getId()) ? 
               this.usuario2 : this.usuario1;
    }

    // Método para atualizar a última atividade
    public void atualizarUltimaAtividade() {
        this.ultimaAtividade = LocalDateTime.now();
    }

    // Método para contar mensagens não lidas de um usuário
    public long contarMensagensNaoLidas(Usuario usuario) {
        if (mensagens == null) return 0;
        
        return mensagens.stream()
                .filter(m -> !m.isLida())
                .filter(m -> !m.getRemetente().getId().equals(usuario.getId()))
                .count();
    }

    // Método para obter a última mensagem
    public Mensagem getUltimaMensagem() {
        if (mensagens == null || mensagens.isEmpty()) {
            return null;
        }
        return mensagens.get(0); // Como está ordenado por dataEnvio DESC
    }
}