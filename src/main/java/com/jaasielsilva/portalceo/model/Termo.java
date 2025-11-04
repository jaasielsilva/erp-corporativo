package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "termos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Termo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(nullable = false, length = 20)
    private String versao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTermo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusTermo status = StatusTermo.RASCUNHO;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_publicacao")
    private LocalDateTime dataPublicacao;

    @Column(name = "data_vigencia_inicio")
    private LocalDateTime dataVigenciaInicio;

    @Column(name = "data_vigencia_fim")
    private LocalDateTime dataVigenciaFim;

    @ManyToOne
    @JoinColumn(name = "criado_por", nullable = false)
    private Usuario criadoPor;

    @ManyToOne
    @JoinColumn(name = "aprovado_por")
    private Usuario aprovadoPor;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "obrigatorio_aceite")
    private boolean obrigatorioAceite = true;

    @Column(name = "notificar_usuarios")
    private boolean notificarUsuarios = true;

    @Column(name = "total_aceites")
    private Long totalAceites = 0L;

    @Column(name = "total_pendentes")
    private Long totalPendentes = 0L;

    public enum TipoTermo {
        TERMOS_USO("Termos de Uso"),
        POLITICA_PRIVACIDADE("Política de Privacidade"),
        CODIGO_CONDUTA("Código de Conduta"),
        POLITICA_SEGURANCA("Política de Segurança"),
        OUTROS("Outros");

        private final String descricao;

        TipoTermo(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusTermo {
        RASCUNHO("Rascunho"),
        PENDENTE_APROVACAO("Pendente de Aprovação"),
        APROVADO("Aprovado"),
        PUBLICADO("Publicado"),
        ARQUIVADO("Arquivado"),
        CANCELADO("Cancelado");

        private final String descricao;

        StatusTermo(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    public boolean isAtivo() {
        return status == StatusTermo.PUBLICADO && 
               (dataVigenciaFim == null || dataVigenciaFim.isAfter(LocalDateTime.now()));
    }

    public boolean isVigente() {
        LocalDateTime agora = LocalDateTime.now();
        return status == StatusTermo.PUBLICADO &&
               (dataVigenciaInicio == null || dataVigenciaInicio.isBefore(agora)) &&
               (dataVigenciaFim == null || dataVigenciaFim.isAfter(agora));
    }
}