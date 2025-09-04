package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.ValeTransporte;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para listagem de Vale Transporte com informações do colaborador
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValeTransporteListDTO {
    
    private Long id;
    private String nomeColaborador;
    private String matriculaColaborador;
    private String departamento;
    private String trajeto;
    private String linhaOnibus;
    private Integer viagensDia;
    private Integer diasUteis;
    private BigDecimal valorTotalMes;
    private BigDecimal valorDesconto;
    private BigDecimal valorSubsidioEmpresa;
    private ValeTransporte.StatusValeTransporte status;
    private LocalDate dataAdesao;
    private String enderecoOrigem;
    private String enderecoDestino;
    
    // Construtor para consultas otimizadas
    public ValeTransporteListDTO(Long id, String nomeColaborador, String matriculaColaborador, 
                                String departamento, String linhaOnibus, Integer viagensDia, 
                                Integer diasUteis, BigDecimal valorTotalMes, BigDecimal valorDesconto, 
                                BigDecimal valorSubsidioEmpresa, ValeTransporte.StatusValeTransporte status,
                                String enderecoOrigem, String enderecoDestino) {
        this.id = id;
        this.nomeColaborador = nomeColaborador;
        this.matriculaColaborador = matriculaColaborador;
        this.departamento = departamento;
        this.linhaOnibus = linhaOnibus;
        this.viagensDia = viagensDia;
        this.diasUteis = diasUteis;
        this.valorTotalMes = valorTotalMes;
        this.valorDesconto = valorDesconto;
        this.valorSubsidioEmpresa = valorSubsidioEmpresa;
        this.status = status;
        this.enderecoOrigem = enderecoOrigem;
        this.enderecoDestino = enderecoDestino;
    }
    
    public String getStatusDescricao() {
        if (status == null) return "";
        switch (status) {
            case ATIVO:
                return "Ativo";
            case SUSPENSO:
                return "Suspenso";
            case CANCELADO:
                return "Cancelado";
            default:
                return status.toString();
        }
    }
    
    public String getTrajetoFormatado() {
        if (enderecoOrigem != null && enderecoDestino != null) {
            return enderecoOrigem + " → " + enderecoDestino;
        } else if (linhaOnibus != null) {
            return "Casa → Trabalho (" + linhaOnibus + ")";
        }
        return "Casa → Trabalho";
    }
    
    public String getStatusCssClass() {
        if (status == null) return "status-indefinido";
        switch (status) {
            case ATIVO:
                return "status-ativo";
            case SUSPENSO:
                return "status-suspenso";
            case CANCELADO:
                return "status-cancelado";
            default:
                return "status-indefinido";
        }
    }
    
    public Integer getTotalViagensmes() {
        return diasUteis != null && viagensDia != null ? diasUteis * viagensDia : 0;
    }
}