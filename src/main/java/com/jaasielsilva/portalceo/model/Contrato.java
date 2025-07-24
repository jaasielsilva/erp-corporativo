package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroContrato;

    @Enumerated(EnumType.STRING)
    private TipoContrato tipo;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusContrato status;

    @Lob
    private String descricao;

    // Relações com @ToString.Exclude para evitar ciclo infinito
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    @ToString.Exclude
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @ToString.Exclude
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_servico_id")
    @ToString.Exclude
    private PrestadorServico prestadorServico;

    // Auditoria
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    @ManyToOne
    @JoinColumn(name = "usuario_edicao_id")
    @ToString.Exclude
    private Usuario editadoPor;

    @ManyToOne
    @JoinColumn(name = "usuario_exclusao_id")
    @ToString.Exclude
    private Usuario usuarioExclusao;

    private LocalDateTime ultimaAtualizacao;

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        ultimaAtualizacao = LocalDateTime.now();
        if (status == null) {
            status = StatusContrato.ATIVO;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        ultimaAtualizacao = LocalDateTime.now();
    }
}
