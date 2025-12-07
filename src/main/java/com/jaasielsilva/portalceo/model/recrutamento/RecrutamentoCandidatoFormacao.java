package com.jaasielsilva.portalceo.model.recrutamento;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rh_recrutamento_candidato_formacoes")
@Getter
@Setter
public class RecrutamentoCandidatoFormacao extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", nullable = false)
    private RecrutamentoCandidato candidato;
    @Column(length = 120)
    private String instituicao;
    @Column(length = 120)
    private String curso;
    @Column(length = 60)
    private String nivel;
    private LocalDate inicio;
    private LocalDate fim;
}

