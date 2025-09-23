package com.jaasielsilva.portalceo.model.ti;

import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_interacao")
public class TicketInteracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O ticket é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketSuporte ticket;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInteracao tipo = TipoInteracao.COMENTARIO;

    @NotNull(message = "O conteúdo é obrigatório")
    @Size(min = 5, max = 2000, message = "O conteúdo deve ter entre 5 e 2000 caracteres")
    @Column(nullable = false, length = 2000)
    private String conteudo;

    @Column(nullable = false)
    private LocalDateTime dataInteracao;

    @Column
    private Boolean visivel = true;

    @Column
    private Boolean interna = false;

    @Column
    private Integer tempoGastoMinutos = 0;

    @Column(length = 500)
    private String observacoes;

    public enum TipoInteracao {
        COMENTARIO("Comentário"),
        ATRIBUICAO("Atribuição"),
        MUDANCA_STATUS("Mudança de Status"),
        MUDANCA_PRIORIDADE("Mudança de Prioridade"),
        SOLUCAO("Solução"),
        ENCAMINHAMENTO("Encaminhamento"),
        REATIVACAO("Reativação"),
        CANCELAMENTO("Cancelamento"),
        ANEXO("Anexo Adicionado"),
        TEMPO_TRABALHADO("Tempo Trabalhado");

        private final String descricao;

        TipoInteracao(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    public void onPrePersist() {
        if (dataInteracao == null) {
            dataInteracao = LocalDateTime.now();
        }
        if (visivel == null) {
            visivel = true;
        }
        if (interna == null) {
            interna = false;
        }
    }

    // Business methods
    public boolean isComentario() {
        return tipo == TipoInteracao.COMENTARIO;
    }

    public boolean isInterno() {
        return interna != null && interna;
    }

    public boolean isVisivel() {
        return visivel != null && visivel;
    }

    public String getIconeCSS() {
        return switch (tipo) {
            case COMENTARIO -> "fas fa-comment";
            case ATRIBUICAO -> "fas fa-user-tag";
            case MUDANCA_STATUS -> "fas fa-exchange-alt";
            case MUDANCA_PRIORIDADE -> "fas fa-exclamation-triangle";
            case SOLUCAO -> "fas fa-check-circle";
            case ENCAMINHAMENTO -> "fas fa-share";
            case REATIVACAO -> "fas fa-redo";
            case CANCELAMENTO -> "fas fa-times-circle";
            case ANEXO -> "fas fa-paperclip";
            case TEMPO_TRABALHADO -> "fas fa-clock";
        };
    }

    public String getCssClass() {
        return switch (tipo) {
            case COMENTARIO -> "info";
            case ATRIBUICAO -> "primary";
            case MUDANCA_STATUS -> "warning";
            case MUDANCA_PRIORIDADE -> "warning";
            case SOLUCAO -> "success";
            case ENCAMINHAMENTO -> "info";
            case REATIVACAO -> "warning";
            case CANCELAMENTO -> "danger";
            case ANEXO -> "secondary";
            case TEMPO_TRABALHADO -> "info";
        };
    }

    public boolean temTempoTrabalhado() {
        return tempoGastoMinutos != null && tempoGastoMinutos > 0;
    }

    public String getTempoTrabalhadoFormatado() {
        if (tempoGastoMinutos == null || tempoGastoMinutos == 0) {
            return "";
        }
        
        int horas = tempoGastoMinutos / 60;
        int minutos = tempoGastoMinutos % 60;
        
        if (horas > 0) {
            return String.format("%dh %dmin", horas, minutos);
        } else {
            return String.format("%dmin", minutos);
        }
    }
}