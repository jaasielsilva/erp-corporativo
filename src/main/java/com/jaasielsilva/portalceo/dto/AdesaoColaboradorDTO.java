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
import java.util.ArrayList;

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

    @Pattern(regexp = "^\\([1-9]{2}\\) [0-9]{4,5}-[0-9]{4}$", message = "Telefone deve ter formato válido (XX) XXXXX-XXXX ou (XX) XXXX-XXXX")
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
    
    private String orgaoEmissorRg; // Órgão emissor do RG
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataEmissaoRg; // Data de emissão do RG

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
        "RG", "CPF", "Comprovante de Endereço", "Foto 3x4"
    );
    
    private List<String> documentosOpcionais = List.of(
        "Carteira de Trabalho", "Título de Eleitor", "Certificado de Reservista",
        "Comprovante de Escolaridade", "Certidão de Nascimento/Casamento",
        "Carteira de Motorista", "Comprovante de Conta Bancária"
    );
    
    // Controle de upload de documentos
    private Map<String, Boolean> statusDocumentos; // documento -> enviado
    private Map<String, String> observacoesDocumentos; // documento -> observação

    // === BENEFÍCIOS ===
    private Map<String, Object> beneficiosSelecionados; // beneficio_id -> configuracao
    
    // Benefícios específicos
    private Boolean planoSaudeOpcional = false;
    private String planoSaudeTipo; // INDIVIDUAL, FAMILIAR
    private Integer planoSaudeDependentes = 0;
    private Long planoSaudeId; // ID do plano selecionado
    
    private Boolean valeRefeicaoOpcional = false;
    private BigDecimal valeRefeicaoValor;
    
    private Boolean valeTransporteOpcional = false;
    private BigDecimal valeTransporteValor;
    
    private Boolean valeAlimentacaoOpcional = false;
    private BigDecimal valeAlimentacaoValor;
    
    // Benefícios adicionais
    private Boolean seguroVidaOpcional = false;
    private Boolean assistenciaOdontologicaOpcional = false;
    private Boolean gymPassOpcional = false;
    
    // Lista de dependentes para benefícios
    private List<DependenteBeneficioDTO> dependentes;

    // === CONTROLE DO PROCESSO ===
    private String sessionId;
    private String etapaAtual = "DADOS_PESSOAIS"; // DADOS_PESSOAIS, DOCUMENTOS, BENEFICIOS, revisao, FINALIZADO
    private String statusProcesso = "EM_ANDAMENTO"; // EM_ANDAMENTO, FINALIZADO, CANCELADO
    
    // Percentual de conclusão do processo
    private Integer percentualConclusao = 0;
    
    // === OBSERVAÇÕES ===
    private String observacoes;
    
    // === DADOS DE CONTROLE ===
    private String criadoPor; // usuário que iniciou o processo
    private LocalDate dataInicioProcesso = LocalDate.now();
    private LocalDate dataFinalizacaoProcesso;
    
    // Dados de auditoria
    private String ultimaAtualizacaoPor;
    private LocalDate dataUltimaAtualizacao;
    
    // Validação de integridade
    private String hashValidacao; // Para validar integridade dos dados
    
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
        return "revisao".equals(etapaAtual) && 
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
            case "BENEFICIOS" -> "revisao";
            case "revisao" -> "FINALIZADO";
            default -> "DADOS_PESSOAIS";
        };
    }
    
    /**
     * Calcula o percentual de conclusão do processo
     */
    public Integer calcularPercentualConclusao() {
        return switch (etapaAtual) {
            case "DADOS_PESSOAIS" -> 25;
            case "DOCUMENTOS" -> 50;
            case "BENEFICIOS" -> 75;
            case "revisao" -> 90;
            case "FINALIZADO" -> 100;
            default -> 0;
        };
    }
    
    /**
     * Verifica se todos os campos obrigatórios dos dados pessoais estão preenchidos
     */
    public boolean isDadosPessoaisCompletos() {
        return nome != null && !nome.trim().isEmpty() &&
               cpf != null && !cpf.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               dataNascimento != null &&
               sexo != null && !sexo.trim().isEmpty() &&
               estadoCivil != null && !estadoCivil.trim().isEmpty() &&
               rg != null && !rg.trim().isEmpty();
    }
    
    /**
     * Verifica se todos os campos obrigatórios dos dados profissionais estão preenchidos
     */
    public boolean isDadosProfissionaisCompletos() {
        return cargoId != null &&
               departamentoId != null &&
               dataAdmissao != null &&
               salario != null && salario.compareTo(BigDecimal.ZERO) > 0 &&
               tipoContrato != null && !tipoContrato.trim().isEmpty() &&
               cargaHoraria != null && cargaHoraria > 0;
    }
    
    /**
     * Verifica se todos os campos obrigatórios do endereço estão preenchidos
     */
    public boolean isEnderecoCompleto() {
        return cep != null && !cep.trim().isEmpty() &&
               logradouro != null && !logradouro.trim().isEmpty() &&
               numero != null && !numero.trim().isEmpty() &&
               bairro != null && !bairro.trim().isEmpty() &&
               cidade != null && !cidade.trim().isEmpty() &&
               estado != null && !estado.trim().isEmpty();
    }
    
    /**
     * Verifica se pode avançar para a próxima etapa
     */
    public boolean podeAvancarEtapa() {
        return switch (etapaAtual) {
            case "DADOS_PESSOAIS" -> isDadosPessoaisCompletos() && isDadosProfissionaisCompletos() && isEnderecoCompleto();
            case "DOCUMENTOS" -> isDocumentosObrigatoriosCompletos();
            case "BENEFICIOS" -> true; // Benefícios são opcionais
            case "revisao" -> isProntoParaFinalizacao();
            default -> false;
        };
    }
    
    /**
     * Inicializa o processo com valores padrão
     */
    public void inicializarProcesso(String usuarioResponsavel) {
        this.sessionId = java.util.UUID.randomUUID().toString();
        this.etapaAtual = "DADOS_PESSOAIS";
        this.statusProcesso = "EM_ANDAMENTO";
        this.criadoPor = usuarioResponsavel;
        this.dataInicioProcesso = LocalDate.now();
        this.percentualConclusao = 0;
        this.pais = "Brasil";
        
        // Inicializar mapas
        this.documentosUpload = new java.util.HashMap<>();
        this.statusDocumentos = new java.util.HashMap<>();
        this.observacoesDocumentos = new java.util.HashMap<>();
        this.beneficiosSelecionados = new java.util.HashMap<>();
        this.dependentes = new ArrayList<>();
        
        // Inicializar status dos documentos obrigatórios
        for (String doc : documentosObrigatorios) {
            statusDocumentos.put(doc, false);
        }
    }
}