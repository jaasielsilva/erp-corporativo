package com.jaasielsilva.portalceo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdesaoColaboradorDTO {

    // === DADOS PESSOAIS ===
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @CPF(message = "CPF deve ter formato válido")
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @Email(message = "Email deve ter formato válido")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    @Pattern(regexp = "^\\([1-9]{2}\\) (?:[2-8]|9[1-9])[0-9]{3}-[0-9]{4}$", message = "Telefone deve ter formato válido")
    private String telefone;

    @NotNull(message = "Sexo é obrigatório")
    private String sexo; // MASCULINO, FEMININO, OUTRO

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    @NotNull(message = "Estado civil é obrigatório")
    private String estadoCivil; // SOLTEIRO, CASADO, DIVORCIADO, VIUVO

    @NotBlank(message = "RG é obrigatório")
    private String rg;

    // === DADOS PROFISSIONAIS ===
    @NotNull(message = "Data de admissão é obrigatória")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataAdmissao;

    @NotNull(message = "Cargo é obrigatório")
    private Long cargoId;

    @NotNull(message = "Departamento é obrigatório")
    private Long departamentoId;

    @NotNull(message = "Salário é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salário deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Salário deve ter no máximo 8 dígitos inteiros e 2 decimais")
    private BigDecimal salario;

    @NotBlank(message = "Tipo de contrato é obrigatório")
    private String tipoContrato; // CLT, PJ, ESTAGIO, TERCEIRIZADO

    @NotNull(message = "Carga horária é obrigatória")
    @Min(value = 1, message = "Carga horária deve ser maior que zero")
    @Max(value = 60, message = "Carga horária não pode exceder 60 horas semanais")
    private Integer cargaHoraria;

    private Long supervisorId;

    // === ENDEREÇO ===
    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "^[0-9]{5}-?[0-9]{3}$", message = "CEP deve ter formato válido")
    private String cep;

    @NotBlank(message = "Logradouro é obrigatório")
    private String logradouro;

    @NotBlank(message = "Número é obrigatório")
    private String numero;

    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    private String estado;

    private String pais = "Brasil";

    // === DOCUMENTOS ===
    private Map<String, String> documentosUpload; // nome_documento -> caminho_arquivo
    
    private List<String> documentosObrigatorios = List.of(
        "RG", "CPF", "Comprovante de Endereço"
    );
    
    private List<String> documentosOpcionais = List.of(
        "Carteira de Trabalho", "Título de Eleitor", "Certificado de Reservista",
        "Comprovante de Escolaridade", "Certidão de Nascimento/Casamento"
    );

    // === BENEFÍCIOS ===
    private Map<String, Object> beneficiosSelecionados; // beneficio_id -> configuracao
    
    // Benefícios específicos
    private Boolean planoSaudeOpcional = false;
    private String planoSaudeTipo; // INDIVIDUAL, FAMILIAR
    private Integer planoSaudeDependentes = 0;
    
    private Boolean valeRefeicaoOpcional = false;
    private BigDecimal valeRefeicaoValor;
    
    private Boolean valeTransporteOpcional = false;
    private BigDecimal valeTransporteValor;
    
    private Boolean valeAlimentacaoOpcional = false;
    private BigDecimal valeAlimentacaoValor;

    // === CONTROLE DO PROCESSO ===
    private String sessionId;
    private String etapaAtual; // DADOS_PESSOAIS, DOCUMENTOS, BENEFICIOS, REVISAO, FINALIZADO
    private String statusProcesso; // EM_ANDAMENTO, FINALIZADO, CANCELADO
    
    // === OBSERVAÇÕES ===
    private String observacoes;
    
    // === DADOS DE CONTROLE ===
    private String criadoPor; // usuário que iniciou o processo
    private LocalDate dataInicioProcesso;
    private LocalDate dataFinalizacaoProcesso;
    
    // === VALIDAÇÕES CUSTOMIZADAS ===
    
    /**
     * Valida se a idade mínima é atendida (16 anos)
     */
    public boolean isIdadeValida() {
        if (dataNascimento == null) return false;
        return LocalDate.now().minusYears(16).isAfter(dataNascimento) || 
               LocalDate.now().minusYears(16).isEqual(dataNascimento);
    }
    
    /**
     * Valida se a data de admissão não é futura
     */
    public boolean isDataAdmissaoValida() {
        if (dataAdmissao == null) return false;
        return !dataAdmissao.isAfter(LocalDate.now());
    }
    
    /**
     * Verifica se todos os documentos obrigatórios foram enviados
     */
    public boolean isDocumentosObrigatoriosCompletos() {
        if (documentosUpload == null || documentosUpload.isEmpty()) {
            return false;
        }
        
        return documentosObrigatorios.stream()
            .allMatch(doc -> documentosUpload.containsKey(doc) && 
                           documentosUpload.get(doc) != null && 
                           !documentosUpload.get(doc).trim().isEmpty());
    }
    
    /**
     * Calcula o valor total dos benefícios selecionados
     */
    public BigDecimal calcularTotalBeneficios() {
        BigDecimal total = BigDecimal.ZERO;
        
        if (valeRefeicaoOpcional && valeRefeicaoValor != null) {
            total = total.add(valeRefeicaoValor);
        }
        
        if (valeTransporteOpcional && valeTransporteValor != null) {
            total = total.add(valeTransporteValor);
        }
        
        if (valeAlimentacaoOpcional && valeAlimentacaoValor != null) {
            total = total.add(valeAlimentacaoValor);
        }
        
        return total;
    }
    
    /**
     * Verifica se o processo está em uma etapa válida para finalização
     */
    public boolean isProntoParaFinalizacao() {
        return "REVISAO".equals(etapaAtual) && 
               isDocumentosObrigatoriosCompletos() &&
               isIdadeValida() &&
               isDataAdmissaoValida();
    }
    
    /**
     * Retorna o próximo passo do processo baseado na etapa atual
     */
    public String getProximaEtapa() {
        return switch (etapaAtual) {
            case "DADOS_PESSOAIS" -> "DOCUMENTOS";
            case "DOCUMENTOS" -> "BENEFICIOS";
            case "BENEFICIOS" -> "REVISAO";
            case "REVISAO" -> "FINALIZADO";
            default -> "DADOS_PESSOAIS";
        };
    }
}