package com.jaasielsilva.portalceo.dto.servicos;

import java.math.BigDecimal;

public class ServicoDTO {
    private Long id;
    private String nome;
    private String descricaoBreve;
    private String categoria;
    private Integer slaRespostaHoras;
    private Integer slaSolucaoHoras;
    private BigDecimal custoBase;

    public ServicoDTO() {}

    public ServicoDTO(Long id, String nome, String descricaoBreve, String categoria,
                      Integer slaRespostaHoras, Integer slaSolucaoHoras, BigDecimal custoBase) {
        this.id = id;
        this.nome = nome;
        this.descricaoBreve = descricaoBreve;
        this.categoria = categoria;
        this.slaRespostaHoras = slaRespostaHoras;
        this.slaSolucaoHoras = slaSolucaoHoras;
        this.custoBase = custoBase;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricaoBreve() { return descricaoBreve; }
    public void setDescricaoBreve(String descricaoBreve) { this.descricaoBreve = descricaoBreve; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Integer getSlaRespostaHoras() { return slaRespostaHoras; }
    public void setSlaRespostaHoras(Integer slaRespostaHoras) { this.slaRespostaHoras = slaRespostaHoras; }
    public Integer getSlaSolucaoHoras() { return slaSolucaoHoras; }
    public void setSlaSolucaoHoras(Integer slaSolucaoHoras) { this.slaSolucaoHoras = slaSolucaoHoras; }
    public BigDecimal getCustoBase() { return custoBase; }
    public void setCustoBase(BigDecimal custoBase) { this.custoBase = custoBase; }
}