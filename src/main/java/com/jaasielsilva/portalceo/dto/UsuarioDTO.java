package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Usuario.Genero;
import com.jaasielsilva.portalceo.model.Usuario.NivelAcesso;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de Usuario
 * Não expõe informações sensíveis como senha
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    @NotBlank(message = "A matrícula é obrigatória")
    @Size(max = 20, message = "A matrícula deve ter no máximo 20 caracteres")
    private String matricula;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
    private String email;

    @NotNull(message = "O gênero é obrigatório")
    private Genero genero;

    @NotNull(message = "O nível de acesso é obrigatório")
    private NivelAcesso nivelAcesso;

    private Boolean ativo;

    private LocalDateTime dataUltimoLogin;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    // Dados do colaborador associado
    private Long colaboradorId;
    private String colaboradorNome;
    private String colaboradorCpf;
    private String colaboradorTelefone;

    // Dados do perfil associado
    private Long perfilId;
    private String perfilNome;

    // Campos para exibição
    private String generoDescricao;
    private String nivelAcessoDescricao;
    private String statusDescricao;

    // Construtor para compatibilidade com código existente
    public UsuarioDTO(Long id, String nome, String email) {
        this.id = id;
        this.colaboradorNome = nome;
        this.email = email;
    }

    // Getter para compatibilidade
    public String getNome() {
        return colaboradorNome;
    }

    public void setNome(String nome) {
        this.colaboradorNome = nome;
    }
}
