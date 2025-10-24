package com.jaasielsilva.portalceo.model.projetos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import com.jaasielsilva.portalceo.model.Colaborador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(name = "projeto_tarefas")
public class TarefaProjeto extends BaseEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @ToString.Include
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa status = StatusTarefa.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridade prioridade = Prioridade.MEDIA;

    private LocalDate prazo;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "atribuida_a_id")
    private Colaborador atribuidaA;

    @Column(nullable = false)
    private Boolean ativo = true;

    public enum StatusTarefa {
        PENDENTE,
        EM_ANDAMENTO,
        CONCLUIDA
    }

    public enum Prioridade {
        BAIXA,
        MEDIA,
        ALTA,
        CRITICA
    }
}
