package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razaoSocial;
    private String nomeFantasia;

    @Column(nullable = false, unique = true)
    private String cnpj;

    private String inscricaoEstadual;
    private String telefone;
    private String celular;
    private String email;
    private String site;

    // Endereço
    private String rua;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    @Column(length = 1000)
    private String observacoes;

    @NotNull(message = "O status é obrigatório.")
    @Column(length = 20)
    private String status;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    // Relacionamento com contratos vinculados a este fornecedor
    @OneToMany(mappedBy = "fornecedor", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Contrato> contratos;


    // Auditoria de criação
    private LocalDateTime dataCriacao;

    // Auditoria de última edição
    private LocalDateTime dataUltimaEdicao;

    @ManyToOne
    @JoinColumn(name = "usuario_edicao_id")
    private Usuario editadoPor;

    // Auditoria de exclusão
    @ManyToOne
    @JoinColumn(name = "usuario_exclusao_id")
    private Usuario usuarioExclusao;

    // Atualiza o status automaticamente quando ativo for setado
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
        if (ativo != null && ativo) {
            this.status = "ATIVO";
        } else {
            this.status = "INATIVO";
        }
    }

    // Opcional: garantir que status sempre sincronize ativo (para quem setar status diretamente)
    public void setStatus(String status) {
        this.status = status != null ? status.toUpperCase() : "INATIVO";
        this.ativo = "ATIVO".equalsIgnoreCase(this.status);
    }

    // Callbacks para setar datas padrão

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = ativo != null && ativo ? "ATIVO" : "INATIVO";
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }
}
