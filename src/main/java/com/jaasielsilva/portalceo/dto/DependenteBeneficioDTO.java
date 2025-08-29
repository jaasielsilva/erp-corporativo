package com.jaasielsilva.portalceo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO para representar dependentes em benefícios durante o processo de adesão
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependenteBeneficioDTO {

    @NotBlank(message = "Nome do dependente é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @CPF(message = "CPF deve ter formato válido")
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    @NotBlank(message = "Parentesco é obrigatório")
    private String parentesco; // CONJUGE, FILHO, FILHA, PAI, MAE, OUTROS

    @NotBlank(message = "Sexo é obrigatório")
    private String sexo; // MASCULINO, FEMININO, OUTRO

    private String rg;
    
    // Campos específicos para benefícios
    private Boolean incluirPlanoSaude = false;
    private Boolean incluirPlanoOdontologico = false;
    private Boolean incluirSeguroVida = false;

    // Estado civil (para cônjuge)
    private String estadoCivil;

    // Dados adicionais
    private String observacoes;

    // Controle
    private Boolean ativo = true;

    /**
     * Calcula a idade do dependente
     */
    public int getIdade() {
        if (dataNascimento == null) return 0;
        return LocalDate.now().getYear() - dataNascimento.getYear();
    }

    /**
     * Verifica se é menor de idade
     */
    public boolean isMenorIdade() {
        return getIdade() < 18;
    }

    /**
     * Verifica se o dependente é elegível para plano de saúde
     */
    public boolean isElegivelPlanoSaude() {
        if (dataNascimento == null) return false;
        
        switch (parentesco.toUpperCase()) {
            case "CONJUGE":
                return true;
            case "FILHO":
            case "FILHA":
                return getIdade() <= 24; // Filhos até 24 anos
            case "PAI":
            case "MAE":
                return getIdade() >= 60; // Pais idosos
            default:
                return false;
        }
    }

    /**
     * Valida se os dados obrigatórios estão preenchidos
     */
    public boolean isDadosCompletos() {
        return nome != null && !nome.trim().isEmpty() &&
               dataNascimento != null &&
               parentesco != null && !parentesco.trim().isEmpty() &&
               sexo != null && !sexo.trim().isEmpty();
    }
}