package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String telefone;
    private String celular;
    private String cpfCnpj;
    private String tipoCliente; // PF ou PJ

    private LocalDate dataNascimento;
    private String rg;
    private String nacionalidade;
    private String estadoCivil;
    private String profissao;
    private String nomeMae;
    private String nomePai;

    // Endereço
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    private LocalDate dataCadastro;

    private String status;

    private String pessoaContato;
    private String observacoes;

    private String nomeFantasia; // Para PJ
    private String inscricaoMunicipal; // Para PJ
    private String inscricaoEstadual; // Para PJ

    private Boolean ativo = true;

    private LocalDateTime dataExclusao; // Exclusão lógica

    // Auditoria de criação
    private LocalDateTime dataCriacao;

    // Auditoria de última edição
    @ManyToOne
    @JoinColumn(name = "usuario_edicao_id")
    private Usuario editadoPor;

    private LocalDateTime dataUltimaEdicao;

    // Campo para controle de último acesso
    private LocalDateTime ultimoAcesso;

    // Campos de Indicação
    private String origem; // "Indicação", "Google", "Redes Sociais", "Outros"
    private String indicadorNome;
    private String indicadorTelefone;
    private LocalDate dataIndicacao;

    // Campo para identificar clientes VIP
    private Boolean vip = false;

    // Relacionamento com contratos vinculados a este cliente
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contrato> contratos;

    // Dados bancários para pagamentos
    @Column(length = 50)
    private String agencia;

    @Column(length = 50)
    private String conta;

    @Column(length = 120)
    private String chavePix;

    // Auditoria de exclusão
    @ManyToOne
    @JoinColumn(name = "usuario_exclusao_id")
    private Usuario usuarioExclusao;

    // Callbacks JPA para setar datas e valores padrão
    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        dataCadastro = LocalDate.now();
        if (status == null) {
            status = "Ativo";
        }
        if (ativo == null) {
            ativo = true;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    public Cliente(Long id, String nome, String nomeFantasia) {
        this.id = id;
        this.nome = nome;
        this.nomeFantasia = nomeFantasia;
    }
}
