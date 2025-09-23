package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Chamado;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de Chamado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoDTO {

    private Long id;
    private String numero;
    private String assunto;
    private String descricao;
    private String prioridade;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAbertura;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataInicioAtendimento;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataResolucao;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataFechamento;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime slaVencimento;
    
    private String tecnicoResponsavel;
    private String solicitanteNome;
    private String solicitanteEmail;
    private String categoria;
    private String subcategoria;
    private Integer tempoResolucaoMinutos;
    private Integer avaliacao;
    private String comentarioAvaliacao;
    private String observacoes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataReabertura;
    
    private Boolean foiReaberto;
    
    // Informações do colaborador responsável (apenas campos essenciais)
    private ColaboradorSimpleDTO colaboradorResponsavel;
    
    // Campos calculados
    private String slaStatus; // VERDE, AMARELO, VERMELHO
    private Long slaRestanteMinutos;
    private String tempoRestante; // Formatado como "2h 30m"
    
    /**
     * DTO simplificado para informações do colaborador
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColaboradorSimpleDTO {
        private Long id;
        private String nome;
        private String email;
        private String cpf;
        private String telefone;
        private String cargoNome;
        private String departamentoNome;
        private Boolean ativo;
    }
    
    /**
     * Construtor para conversão direta de Chamado para ChamadoDTO
     */
    public ChamadoDTO(Chamado chamado) {
        this.id = chamado.getId();
        this.numero = chamado.getNumero();
        this.assunto = chamado.getAssunto();
        this.descricao = chamado.getDescricao();
        this.prioridade = chamado.getPrioridade() != null ? chamado.getPrioridade().name() : null;
        this.status = chamado.getStatus() != null ? chamado.getStatus().name() : null;
        this.dataAbertura = chamado.getDataAbertura();
        this.dataInicioAtendimento = chamado.getDataInicioAtendimento();
        this.dataResolucao = chamado.getDataResolucao();
        this.dataFechamento = chamado.getDataFechamento();
        this.slaVencimento = chamado.getSlaVencimento();
        this.tecnicoResponsavel = chamado.getTecnicoResponsavel();
        this.solicitanteNome = chamado.getSolicitanteNome();
        this.solicitanteEmail = chamado.getSolicitanteEmail();
        this.categoria = chamado.getCategoria();
        this.subcategoria = chamado.getSubcategoria();
        this.tempoResolucaoMinutos = chamado.getTempoResolucaoMinutos();
        this.avaliacao = chamado.getAvaliacao();
        this.comentarioAvaliacao = chamado.getComentarioAvaliacao();
        this.observacoes = chamado.getObservacoes();
        this.dataReabertura = chamado.getDataReabertura();
        this.foiReaberto = chamado.getFoiReaberto();
        
        // Converter colaborador responsável (se existir e estiver carregado)
        if (chamado.getColaboradorResponsavel() != null) {
            try {
                var colaborador = chamado.getColaboradorResponsavel();
                this.colaboradorResponsavel = new ColaboradorSimpleDTO(
                    colaborador.getId(),
                    colaborador.getNome(),
                    colaborador.getEmail(),
                    colaborador.getCpf(),
                    colaborador.getTelefone(),
                    colaborador.getCargo() != null ? colaborador.getCargo().getNome() : null,
                    colaborador.getDepartamento() != null ? colaborador.getDepartamento().getNome() : null,
                    colaborador.getAtivo()
                );
            } catch (Exception e) {
                // Se houver erro ao acessar o colaborador (lazy loading), deixar null
                this.colaboradorResponsavel = null;
            }
        }
        
        // Calcular campos derivados
        calcularCamposDerivados();
    }
    
    /**
     * Calcula campos derivados como SLA status e tempo restante
     */
    private void calcularCamposDerivados() {
        if (slaVencimento != null) {
            LocalDateTime agora = LocalDateTime.now();
            
            if (agora.isAfter(slaVencimento)) {
                this.slaStatus = "VERMELHO";
                this.slaRestanteMinutos = 0L;
                this.tempoRestante = "Vencido";
            } else {
                long minutosRestantes = java.time.Duration.between(agora, slaVencimento).toMinutes();
                this.slaRestanteMinutos = minutosRestantes;
                
                // Determinar cor do SLA baseado no tempo restante
                long horasRestantes = minutosRestantes / 60;
                if (horasRestantes <= 2) {
                    this.slaStatus = "VERMELHO";
                } else if (horasRestantes <= 8) {
                    this.slaStatus = "AMARELO";
                } else {
                    this.slaStatus = "VERDE";
                }
                
                // Formatar tempo restante
                if (horasRestantes > 0) {
                    long minutosResto = minutosRestantes % 60;
                    this.tempoRestante = horasRestantes + "h " + minutosResto + "m";
                } else {
                    this.tempoRestante = minutosRestantes + "m";
                }
            }
        } else {
            this.slaStatus = "INDEFINIDO";
            this.slaRestanteMinutos = null;
            this.tempoRestante = "Não definido";
        }
    }
    
    /**
     * Método estático para conversão de lista de Chamados para lista de DTOs
     */
    public static java.util.List<ChamadoDTO> fromList(java.util.List<com.jaasielsilva.portalceo.model.Chamado> chamados) {
        return chamados.stream()
                .map(ChamadoDTO::new)
                .collect(java.util.stream.Collectors.toList());
    }
}