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

    private String nomeFantasia;          // Para PJ
    private String inscricaoMunicipal;    // Para PJ
    private String inscricaoEstadual;     // Para PJ

    private Boolean ativo = true;

    private LocalDateTime dataExclusao;       // Exclusão lógica

    // Auditoria de criação
    private LocalDateTime dataCriacao;

    // Auditoria de última edição
    @ManyToOne
    @JoinColumn(name = "usuario_edicao_id")
    private Usuario editadoPor;

    private LocalDateTime dataUltimaEdicao;

    @OneToMany(mappedBy = "cliente")
    private List<Venda> vendas;

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
}
