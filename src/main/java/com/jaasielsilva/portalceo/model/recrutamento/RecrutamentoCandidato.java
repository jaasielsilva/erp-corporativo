package com.jaasielsilva.portalceo.model.recrutamento;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@Entity
@Table(name = "rh_recrutamento_candidatos", indexes = {
        @Index(name = "idx_candidato_nome", columnList = "nome"),
        @Index(name = "idx_candidato_email", columnList = "email"),
        @Index(name = "idx_candidato_telefone", columnList = "telefone"),
        @Index(name = "idx_candidato_genero", columnList = "genero"),
        @Index(name = "idx_candidato_nascimento", columnList = "dataNascimento")
})
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class RecrutamentoCandidato extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(length = 150)
    private String email;
    @Column(length = 30)
    private String telefone;
    @Column(length = 20)
    private String genero;
    private LocalDate dataNascimento;
}

