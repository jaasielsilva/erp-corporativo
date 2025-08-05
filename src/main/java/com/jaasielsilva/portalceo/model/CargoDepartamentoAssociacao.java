package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidade para validar associações entre cargos e departamentos
 * Define quais cargos são válidos para cada departamento
 */
@Entity
@Table(name = "cargo_departamento_associacao", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"cargo_id", "departamento_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargoDepartamentoAssociacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Cargo associado
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", nullable = false)
    private Cargo cargo;
    
    /**
     * Departamento associado
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;
    
    /**
     * Indica se a associação está ativa
     */
    @Column(name = "ativo")
    private Boolean ativo = true;
    
    /**
     * Indica se é uma associação obrigatória (cargo só pode existir neste departamento)
     */
    @Column(name = "obrigatorio")
    private Boolean obrigatorio = false;
    
    /**
     * Observações sobre a associação
     */
    @Column(name = "observacoes")
    private String observacoes;
}