package com.jaasielsilva.portalceo.model.recrutamento;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_recrutamento_candidato_habilidades")
@Getter
@Setter
public class RecrutamentoCandidatoHabilidade extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", nullable = false)
    private RecrutamentoCandidato candidato;
    @Column(length = 120)
    private String nome;
    @Column(length = 30)
    private String nivel;
}

