package com.jaasielsilva.portalceo.model.treinamentos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_treinamentos_instrutores", indexes = {
        @Index(name = "idx_treinamento_instrutor_nome", columnList = "nome")
})
@Getter
@Setter
public class TreinamentoInstrutor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(length = 150)
    private String email;
    @Column(columnDefinition = "TEXT")
    private String bio;
}

