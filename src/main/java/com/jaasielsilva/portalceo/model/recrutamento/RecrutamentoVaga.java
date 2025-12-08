package com.jaasielsilva.portalceo.model.recrutamento;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "rh_recrutamento_vagas")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class RecrutamentoVaga extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String titulo;
    @Column(columnDefinition = "TEXT")
    private String descricao;
    @Column(length = 80)
    private String departamento;
    @Column(length = 60)
    private String senioridade;
    @Column(length = 80)
    private String localidade;
    @Column(length = 60)
    private String tipoContrato;
    @Column(length = 20)
    private String status;
    @Column(columnDefinition = "TEXT")
    private String responsabilidades;
    @Column(columnDefinition = "TEXT")
    private String requisitos;
    @Column(columnDefinition = "TEXT")
    private String diferenciais;
    @Column(columnDefinition = "TEXT")
    private String beneficios;
}
