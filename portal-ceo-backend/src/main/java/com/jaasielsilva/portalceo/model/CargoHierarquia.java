package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidade para representar a hierarquia entre cargos
 * Permite definir relacionamentos hierárquicos como supervisor/subordinado
 */
@Entity
@Table(name = "cargo_hierarquia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargoHierarquia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Cargo superior na hierarquia
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_superior_id", nullable = false)
    private Cargo cargoSuperior;
    
    /**
     * Cargo subordinado na hierarquia
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_subordinado_id", nullable = false)
    private Cargo cargoSubordinado;
    
    /**
     * Nível hierárquico (1 = mais alto, números maiores = níveis mais baixos)
     */
    @Column(name = "nivel_hierarquico")
    private Integer nivelHierarquico;
    
    /**
     * Indica se a relação está ativa
     */
    @Column(name = "ativo")
    private Boolean ativo = true;
    
    /**
     * Descrição da relação hierárquica
     */
    @Column(name = "descricao")
    private String descricao;
}