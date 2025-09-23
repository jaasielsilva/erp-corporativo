package com.jaasielsilva.portalceo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private String nome;

    @Column(unique = true)
    private String ean;

    private String codigoInterno;

    private String descricao;

    @NotNull
    @Column(precision = 10, scale = 2)
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

    private boolean ativo = true;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "data_ultima_compra")
    private LocalDateTime dataUltimaCompra;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @PrePersist
    public void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
        if (ativo) {
            ativo = true;
        }
    }

    public boolean precisaRepor() {
        return estoque != null && minimoEstoque != null && estoque <= minimoEstoque;
    }
}