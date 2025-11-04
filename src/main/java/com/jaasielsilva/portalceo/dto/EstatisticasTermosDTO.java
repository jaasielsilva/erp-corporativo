package com.jaasielsilva.portalceo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para estatísticas do módulo de termos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasTermosDTO {

    // Estatísticas gerais
    private Long totalTermos;
    private Long termosAtivos;
    private Long termosRascunho;
    private Long termosArquivados;

    // Estatísticas de aceites
    private Long totalAceites;
    private Long aceitesHoje;
    private Long aceitesSemana;
    private Long aceitesMes;

    // Estatísticas de usuários
    private Long totalUsuarios;
    private Long usuariosComAceitesPendentes;
    private Long usuariosComTodosAceites;

    // Percentuais
    private Double percentualAdesao;
    private Double percentualPendentes;

    // Termo mais recente
    private String termoMaisRecenteTitulo;
    private String termoMaisRecenteVersao;
    private LocalDateTime termoMaisRecenteData;

    // Termo com mais aceites
    private String termoMaisAceitoTitulo;
    private Long termoMaisAceitoQuantidade;

    // Atividade recente
    private List<AtividadeRecenteDTO> atividadesRecentes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtividadeRecenteDTO {
        private String tipo; // "aceite", "criacao", "publicacao"
        private String descricao;
        private String usuarioNome;
        private LocalDateTime dataHora;
        private String icone;
        private String cor;
    }

    public void calcularPercentuais() {
        if (totalUsuarios != null && totalUsuarios > 0) {
            long usuariosComAceites = usuariosComTodosAceites != null ? usuariosComTodosAceites : 0L;
            long usuariosPendentes = usuariosComAceitesPendentes != null ? usuariosComAceitesPendentes : 0L;
            
            this.percentualAdesao = (usuariosComAceites * 100.0) / totalUsuarios;
            this.percentualPendentes = (usuariosPendentes * 100.0) / totalUsuarios;
        } else {
            this.percentualAdesao = 0.0;
            this.percentualPendentes = 0.0;
        }
    }
}