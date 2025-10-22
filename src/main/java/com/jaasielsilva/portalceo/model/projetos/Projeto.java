package com.jaasielsilva.portalceo.model.projetos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import com.jaasielsilva.portalceo.model.Colaborador;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "projetos")
public class Projeto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProjeto status = StatusProjeto.EM_ANDAMENTO;

    private LocalDate prazo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Colaborador responsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private EquipeProjeto equipe;

    private Integer progresso = 0;

    private BigDecimal orcamento;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TarefaProjeto> tarefas = new ArrayList<>();

    @Column(nullable = false)
    private Boolean ativo = true;

    public enum StatusProjeto {
        PLANEJADO,
        EM_ANDAMENTO,
        CONCLUIDO,
        ATRASADO,
        CANCELADO
    }
}
