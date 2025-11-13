package com.jaasielsilva.portalceo.model.servicos;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 400)
    private String descricaoBreve;

    @Column(length = 80)
    private String categoria;

    @Column
    private Integer slaRespostaHoras;

    @Column
    private Integer slaSolucaoHoras;

    @Column(precision = 12, scale = 2)
    private BigDecimal custoBase;

    @Column
    private boolean exigeAprovacao;

    @Column
    private boolean ativo = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricaoBreve() {
        return descricaoBreve;
    }

    public void setDescricaoBreve(String descricaoBreve) {
        this.descricaoBreve = descricaoBreve;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getSlaRespostaHoras() {
        return slaRespostaHoras;
    }

    public void setSlaRespostaHoras(Integer slaRespostaHoras) {
        this.slaRespostaHoras = slaRespostaHoras;
    }

    public Integer getSlaSolucaoHoras() {
        return slaSolucaoHoras;
    }

    public void setSlaSolucaoHoras(Integer slaSolucaoHoras) {
        this.slaSolucaoHoras = slaSolucaoHoras;
    }

    public BigDecimal getCustoBase() {
        return custoBase;
    }

    public void setCustoBase(BigDecimal custoBase) {
        this.custoBase = custoBase;
    }

    public boolean isExigeAprovacao() {
        return exigeAprovacao;
    }

    public void setExigeAprovacao(boolean exigeAprovacao) {
        this.exigeAprovacao = exigeAprovacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}