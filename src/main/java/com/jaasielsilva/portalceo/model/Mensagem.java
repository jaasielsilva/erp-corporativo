package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    @Column(name = "lida", nullable = false)
    @Builder.Default
    private boolean lida = false;

    @Column(name = "data_leitura")
    private LocalDateTime dataLeitura;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    @Builder.Default
    private TipoMensagem tipo = TipoMensagem.TEXTO;

    public enum TipoMensagem {
        TEXTO,
        ARQUIVO,
        IMAGEM,
        SISTEMA
    }

    @PrePersist
    protected void onCreate() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }

    // Método para marcar como lida
    public void marcarComoLida() {
        this.lida = true;
        this.dataLeitura = LocalDateTime.now();
    }

    // Método para verificar se a mensagem é do usuário
    public boolean isDoUsuario(Usuario usuario) {
        return this.remetente.getId().equals(usuario.getId());
    }

    // Método para obter o outro participante da conversa
    public Usuario getOutroParticipante(Usuario usuarioAtual) {
        return this.remetente.getId().equals(usuarioAtual.getId()) ? this.destinatario : this.remetente;
    }
}