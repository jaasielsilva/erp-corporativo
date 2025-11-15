package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.ContratoLegal;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContratoLegalDTO {
    private Long id;
    private String numeroContrato;
    private String titulo;
    private String descricao;
    private ContratoLegal.TipoContrato tipo;
    private ContratoLegal.StatusContrato status;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate dataVencimento;
    private BigDecimal valorMensal;
    private BigDecimal valorContrato;
    private String contraparte;
    private String usuarioResponsavel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroContrato() { return numeroContrato; }
    public void setNumeroContrato(String numeroContrato) { this.numeroContrato = numeroContrato; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public ContratoLegal.TipoContrato getTipo() { return tipo; }
    public void setTipo(ContratoLegal.TipoContrato tipo) { this.tipo = tipo; }
    public ContratoLegal.StatusContrato getStatus() { return status; }
    public void setStatus(ContratoLegal.StatusContrato status) { this.status = status; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public BigDecimal getValorMensal() { return valorMensal; }
    public void setValorMensal(BigDecimal valorMensal) { this.valorMensal = valorMensal; }
    public BigDecimal getValorContrato() { return valorContrato; }
    public void setValorContrato(BigDecimal valorContrato) { this.valorContrato = valorContrato; }
    public String getContraparte() { return contraparte; }
    public void setContraparte(String contraparte) { this.contraparte = contraparte; }
    public String getUsuarioResponsavel() { return usuarioResponsavel; }
    public void setUsuarioResponsavel(String usuarioResponsavel) { this.usuarioResponsavel = usuarioResponsavel; }
}