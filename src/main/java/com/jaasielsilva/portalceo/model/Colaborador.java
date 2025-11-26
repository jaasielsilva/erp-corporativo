package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "colaboradores", indexes = {
    @Index(name = "idx_colaboradores_nome", columnList = "nome"),
    @Index(name = "idx_colaboradores_departamento", columnList = "departamento_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Colaborador {

    // Construtor para consultas otimizadas
    public Colaborador(Long id, String nome, String email, String cpf) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.ativo = true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @CPF(message = "CPF deve ter formato válido")
    @NotBlank(message = "CPF é obrigatório")
    @Column(unique = true, nullable = false, length = 14)
    private String cpf;

    @Email(message = "Email deve ter formato válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^\\([1-9]{2}\\) (?:[2-8]|9[1-9])[0-9]{3}-[0-9]{4}$", message = "Telefone deve ter formato válido")
    private String telefone;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    @Column(name = "data_admissao", nullable = false)
    @NotNull(message = "Data de admissão é obrigatória")
    @PastOrPresent(message = "Data de admissão não pode ser futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataAdmissao;

    @Enumerated(EnumType.STRING)
    private EstadoCivil estadoCivil;

    @Enumerated(EnumType.STRING)
    private StatusColaborador status = StatusColaborador.ATIVO;

    private Boolean ativo = true;

    private LocalDateTime dataCriacao;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @ManyToOne
    @JoinColumn(name = "cargo_id")
    @JsonIgnoreProperties({ "colaboradores", "departamentos" })
    private Cargo cargo;

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ColaboradorBeneficio> beneficios;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    @JsonIgnoreProperties({ "colaboradores", "cargos" })
    private Departamento departamento;

    private LocalDateTime dataUltimaEdicao;

    @Column(name = "rg")
    private String rg;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Salário deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Salário deve ter no máximo 8 dígitos inteiros e 2 decimais")
    private BigDecimal salario;

    // Campos de endereço
    @Pattern(regexp = "^[0-9]{5}-?[0-9]{3}$", message = "CEP deve ter formato válido")
    private String cep;

    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String pais;

    private String observacoes;

    private String tipoContrato;

    @Min(value = 1, message = "Carga horária deve ser maior que zero")
    @Max(value = 60, message = "Carga horária não pode exceder 60 horas semanais")
    private Integer cargaHoraria;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    @JsonIgnoreProperties({ "supervisor", "cargo", "departamento" })
    private Colaborador supervisor;

    @OneToOne(mappedBy = "colaborador", fetch = FetchType.LAZY)
    @JsonIgnore
    private Usuario usuario;

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HistoricoColaborador> historico;

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null)
            ativo = true;
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    // Apenas ID no equals/hashCode para evitar loop
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Colaborador))
            return false;
        Colaborador that = (Colaborador) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Enums internas ou separadas em arquivos próprios
    public enum Sexo {
        MASCULINO, FEMININO, OUTRO
    }

    public enum EstadoCivil {
        SOLTEIRO, CASADO, DIVORCIADO, VIUVO
    }

    public enum StatusColaborador {
        ATIVO, INATIVO, SUSPENSO
    }
}
