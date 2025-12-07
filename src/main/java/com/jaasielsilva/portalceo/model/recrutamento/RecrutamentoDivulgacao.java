package com.jaasielsilva.portalceo.model.recrutamento;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_recrutamento_divulgacoes")
@Getter
@Setter
public class RecrutamentoDivulgacao extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private RecrutamentoVaga vaga;
    @Column(length = 80)
    private String plataforma;
    @Column(length = 200)
    private String url;
}

