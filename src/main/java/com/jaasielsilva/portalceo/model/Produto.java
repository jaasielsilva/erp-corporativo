package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String ean;

    private String codigoInterno;

    private String descricao;

    private BigDecimal preco;

    private Integer estoque;

    private Integer minimoEstoque;

    private String unidadeMedida;

    private String marca;

    private BigDecimal peso;

    private BigDecimal largura;

    private BigDecimal altura;

    private BigDecimal profundidade;

    private String imagemUrl;

    private boolean ativo;

    private LocalDateTime dataCadastro;

    private LocalDateTime dataUltimaCompra;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @PrePersist
    public void prePersist() {
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
    }
}
