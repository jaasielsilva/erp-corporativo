package com.jaasielsilva.portalceo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO específico para criação de Política de Segurança via /termos/api/politica-seguranca
 * Não exige o campo 'tipo' porque é definido como POLITICA_SEGURANCA no controller.
 */
@Data
public class PoliticaSegurancaCriacaoDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
    private String titulo;

    @NotBlank(message = "O conteúdo é obrigatório")
    private String conteudo;

    @NotBlank(message = "A versão é obrigatória")
    @Size(max = 20, message = "A versão deve ter no máximo 20 caracteres")
    private String versao;

    private String observacoes;
    private boolean obrigatorioAceite;
    private boolean notificarUsuarios;

    private LocalDateTime dataVigenciaInicio;
    private LocalDateTime dataVigenciaFim;
}