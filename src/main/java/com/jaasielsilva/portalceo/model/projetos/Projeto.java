package com.jaasielsilva.portalceo.model.projetos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import com.jaasielsilva.portalceo.model.Colaborador;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "projetos")
public class Projeto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do projeto é obrigatório")
    @Size(max = 255, message = "O nome do projeto não pode exceder 255 caracteres")
    @Column(nullable = false)
    private String nome;

    @Size(max = 2000, message = "A descrição do projeto não pode exceder 2000 caracteres")
    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProjeto status = StatusProjeto.EM_ANDAMENTO;

    @NotNull(message = "O prazo do projeto é obrigatório")
    private LocalDate prazo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Colaborador responsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private EquipeProjeto equipe;

    @Min(value = 0, message = "O progresso não pode ser negativo")
    @Max(value = 100, message = "O progresso não pode exceder 100%")
    private Integer progresso = 0;

    @Min(value = 0, message = "O orçamento não pode ser negativo")
    private BigDecimal orcamento;

    @OneToMany(mappedBy = "projeto", orphanRemoval = true)
    private Set<TarefaProjeto> tarefas = new HashSet<>();

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
