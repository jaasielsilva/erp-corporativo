package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Termo;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de Termo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoDTO {

    private Long id;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
    private String titulo;

    @NotBlank(message = "O conteúdo é obrigatório")
    private String conteudo;

    @NotBlank(message = "A versão é obrigatória")
    @Size(max = 20, message = "A versão deve ter no máximo 20 caracteres")
    private String versao;

    @NotNull(message = "O tipo do termo é obrigatório")
    private Termo.TipoTermo tipo;

    private Termo.StatusTermo status;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataPublicacao;
    private LocalDateTime dataVigenciaInicio;
    private LocalDateTime dataVigenciaFim;

    // Dados do usuário que criou
    private Long criadoPorId;
    private String criadoPorNome;

    // Dados do usuário que aprovou
    private Long aprovadoPorId;
    private String aprovadoPorNome;
    private LocalDateTime dataAprovacao;

    private String observacoes;
    private boolean obrigatorioAceite;
    private boolean notificarUsuarios;

    // Estatísticas
    private Long totalAceites;
    private Long totalPendentes;
    private Double percentualAceite;

    // Campos para exibição
    private String tipoDescricao;
    private String statusDescricao;
    private boolean ativo;
    private boolean vigente;

    // Construtor para listagem simples
    public TermoDTO(Long id, String titulo, String versao, Termo.TipoTermo tipo, 
                   Termo.StatusTermo status, LocalDateTime dataCriacao) {
        this.id = id;
        this.titulo = titulo;
        this.versao = versao;
        this.tipo = tipo;
        this.status = status;
        this.dataCriacao = dataCriacao;
        this.tipoDescricao = tipo != null ? tipo.getDescricao() : "";
        this.statusDescricao = status != null ? status.getDescricao() : "";
    }

    // Construtor para estatísticas
    public TermoDTO(Long id, String titulo, String versao, Long totalAceites, Long totalPendentes) {
        this.id = id;
        this.titulo = titulo;
        this.versao = versao;
        this.totalAceites = totalAceites != null ? totalAceites : 0L;
        this.totalPendentes = totalPendentes != null ? totalPendentes : 0L;
        
        long total = this.totalAceites + this.totalPendentes;
        this.percentualAceite = total > 0 ? (this.totalAceites * 100.0) / total : 0.0;
    }

    public void calcularPercentualAceite() {
        long total = (totalAceites != null ? totalAceites : 0L) + 
                    (totalPendentes != null ? totalPendentes : 0L);
        this.percentualAceite = total > 0 ? ((totalAceites != null ? totalAceites : 0L) * 100.0) / total : 0.0;
    }
}