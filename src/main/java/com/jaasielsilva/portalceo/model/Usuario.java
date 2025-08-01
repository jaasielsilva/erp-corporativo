package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, unique = true)
    private String matricula;
    
    @ManyToOne
    private Colaborador colaborador;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(length = 20)
    private String telefone;

    @Column(length = 14, unique = true)
    private String cpf;

    private String cep;

    private String endereco;

    private String cidade;

    private String estado;

    @Column(length = 10)
    private String ramal;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Genero genero;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private NivelAcesso nivelAcesso;

    @Lob
    @Column(name = "foto_perfil", columnDefinition = "LONGBLOB")
    private byte[] fotoPerfil;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_perfil",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis;

    // Enum com os status possíveis
    public enum Status {
        ATIVO,
        INATIVO,
        BLOQUEADO,
        DEMITIDO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ATIVO;

    // Relacionamento ManyToOne para Cargo e Departamento
    @ManyToOne
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

    @Column(name = "data_desligamento")
    private LocalDate dataDesligamento;

}
