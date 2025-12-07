package com.jaasielsilva.portalceo.model.treinamentos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_treinamentos_cursos", indexes = {
        @Index(name = "idx_treinamento_curso_titulo", columnList = "titulo")
})
@Getter
@Setter
public class TreinamentoCurso extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String titulo;
    @Column(columnDefinition = "TEXT")
    private String descricao;
    @Column(length = 60)
    private String categoria;
    private Integer cargaHoraria;
}

