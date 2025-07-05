package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Genero;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UsuarioFormDTO {

    private Long id;

    @Size(max = 30)
    private String matricula;

    @NotBlank
    private String nome;

    @Email
    @NotBlank
    private String email;

    private String senha;

    private String telefone;

    private String cpf;

    private LocalDate dataNascimento;

    private String departamento;

    private String cargo;

    private LocalDate dataAdmissao;

    private String cep;

    private String endereco;

    private String cidade;

    private String estado;

    private String ramal;

    private LocalDate dataDemissao;

    private Genero genero;

    private NivelAcesso nivelAcesso;

    @NotEmpty(message = "Selecione pelo menos um perfil")
    private Set<Long> perfilIds;

    // Getters e Setters
    // (Gerar automaticamente ou usar Lombok se preferir)
}
