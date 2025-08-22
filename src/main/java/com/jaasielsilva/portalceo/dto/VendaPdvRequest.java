package com.jaasielsilva.portalceo.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class VendaPdvRequest {
    private Long clienteId;
    private String formaPagamento;
    private Integer parcelas;
    private List<ItemRequest> itens;
    private String Observacoes;
    private BigDecimal Desconto;
    private BigDecimal ValorPago;


    // Classe interna para itens
    public static class ItemRequest {
        private String ean;
        private Integer quantidade;

        // Getters e Setters
        public String getEan() {
            return ean;
        }

        public void setEan(String ean) {
            this.ean = ean;
        }

        public Integer getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }
    }
}