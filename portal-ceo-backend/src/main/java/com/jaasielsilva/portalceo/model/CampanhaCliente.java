package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "campanha_cliente")
public class CampanhaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id", nullable = false)
    private CampanhaMarketing campanha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEnvio statusEnvio = StatusEnvio.PENDENTE;

    @Column
    private LocalDateTime dataEnvio;

    @Column
    private LocalDateTime dataAbertura;

    @Column
    private LocalDateTime dataClique;

    @Column
    private LocalDateTime dataConversao;

    @Column
    private Integer numeroAberturas = 0;

    @Column
    private Integer numeroCliques = 0;

    @Column
    private Boolean converteu = false;

    @Column
    private boolean comprou = false;

    @Column(length = 500)
    private String observacoes;

    @Column(length = 200)
    private String motivoFalha;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum StatusEnvio {
        PENDENTE("Pendente"),
        ENVIADO("Enviado"),
        ENTREGUE("Entregue"),
        ABERTO("Aberto"),
        CLICOU("Clicou"),
        CONVERTEU("Converteu"),
        FALHA("Falha no Envio"),
        REJEITADO("Rejeitado"),
        DESCADASTRADO("Descadastrado");

        private final String descricao;

        StatusEnvio(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (statusEnvio == null) {
            statusEnvio = StatusEnvio.PENDENTE;
        }
        if (numeroAberturas == null) {
            numeroAberturas = 0;
        }
        if (numeroCliques == null) {
            numeroCliques = 0;
        }
        if (converteu == null) {
            converteu = false;
        }
        if (!comprou) {
            comprou = false;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    // Business methods
    public boolean isPendente() {
        return statusEnvio == StatusEnvio.PENDENTE;
    }

    public boolean isEnviado() {
        return statusEnvio == StatusEnvio.ENVIADO || statusEnvio == StatusEnvio.ENTREGUE;
    }

    public boolean isAberto() {
        return statusEnvio == StatusEnvio.ABERTO ||
                statusEnvio == StatusEnvio.CLICOU ||
                statusEnvio == StatusEnvio.CONVERTEU;
    }

    public boolean isClicou() {
        return statusEnvio == StatusEnvio.CLICOU || statusEnvio == StatusEnvio.CONVERTEU;
    }

    public boolean isConverteu() {
        return statusEnvio == StatusEnvio.CONVERTEU || converteu;
    }

    public boolean isFalha() {
        return statusEnvio == StatusEnvio.FALHA || statusEnvio == StatusEnvio.REJEITADO;
    }

    public void marcarComoEnviado() {
        this.statusEnvio = StatusEnvio.ENVIADO;
        this.dataEnvio = LocalDateTime.now();
    }

    public void marcarComoAberto() {
        if (this.dataAbertura == null) {
            this.dataAbertura = LocalDateTime.now();
        }
        this.statusEnvio = StatusEnvio.ABERTO;
        this.numeroAberturas++;
    }

    public void marcarComoClicado() {
        if (this.dataClique == null) {
            this.dataClique = LocalDateTime.now();
        }
        this.statusEnvio = StatusEnvio.CLICOU;
        this.numeroCliques++;
    }

    public void marcarComoConvertido() {
        if (this.dataConversao == null) {
            this.dataConversao = LocalDateTime.now();
        }
        this.statusEnvio = StatusEnvio.CONVERTEU;
        this.converteu = true;
    }

    public void marcarComoComprou() {
        this.comprou = true;
        if (!this.converteu) {
            marcarComoConvertido();
        }
    }

    public void marcarComoFalha(String motivo) {
        this.statusEnvio = StatusEnvio.FALHA;
        this.motivoFalha = motivo;
    }

    public long getTempoParaAbertura() {
        if (dataEnvio != null && dataAbertura != null) {
            return java.time.Duration.between(dataEnvio, dataAbertura).toMinutes();
        }
        return 0;
    }

    public long getTempoParaClique() {
        if (dataAbertura != null && dataClique != null) {
            return java.time.Duration.between(dataAbertura, dataClique).toMinutes();
        }
        return 0;
    }

    public long getTempoParaConversao() {
        if (dataClique != null && dataConversao != null) {
            return java.time.Duration.between(dataClique, dataConversao).toHours();
        }
        return 0;
    }
}