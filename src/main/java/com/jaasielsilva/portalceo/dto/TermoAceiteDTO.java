package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.TermoAceite;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de TermoAceite
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoAceiteDTO {

    private Long id;

    // Dados do termo
    private Long termoId;
    private String termoTitulo;
    private String termoVersao;
    private String termoTipo;

    // Dados do usuário
    private Long usuarioId;
    private String usuarioNome;
    private String usuarioEmail;
    private String usuarioMatricula;

    private LocalDateTime dataAceite;
    private String ipAceite;
    private String userAgent;
    private TermoAceite.StatusAceite status;
    private String observacoes;
    private String versaoTermo;
    private String assinaturaDigital;

    // Campos para exibição
    private String statusDescricao;
    private boolean valido;
    private String tempoAceite; // Ex: "há 2 dias"

    // Construtor para listagem simples
    public TermoAceiteDTO(Long id, String termoTitulo, String usuarioNome, 
                         LocalDateTime dataAceite, TermoAceite.StatusAceite status) {
        this.id = id;
        this.termoTitulo = termoTitulo;
        this.usuarioNome = usuarioNome;
        this.dataAceite = dataAceite;
        this.status = status;
        this.statusDescricao = status != null ? status.getDescricao() : "";
    }

    // Construtor para relatórios
    public TermoAceiteDTO(String usuarioNome, String usuarioEmail, String usuarioMatricula,
                         String termoTitulo, String termoVersao, LocalDateTime dataAceite,
                         TermoAceite.StatusAceite status) {
        this.usuarioNome = usuarioNome;
        this.usuarioEmail = usuarioEmail;
        this.usuarioMatricula = usuarioMatricula;
        this.termoTitulo = termoTitulo;
        this.termoVersao = termoVersao;
        this.dataAceite = dataAceite;
        this.status = status;
        this.statusDescricao = status != null ? status.getDescricao() : "";
    }
}