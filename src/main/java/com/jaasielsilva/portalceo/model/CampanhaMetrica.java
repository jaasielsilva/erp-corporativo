package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "campanha_metrica")
public class CampanhaMetrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id", nullable = false)
    private CampanhaMarketing campanha;

    @Column(nullable = false)
    private LocalDate dataMetrica;

    @Column(nullable = false)
    private Integer envios = 0;

    @Column(nullable = false)
    private Integer entregues = 0;

    @Column(nullable = false)
    private Integer aberturas = 0;

    @Column(nullable = false)
    private Integer cliques = 0;

    @Column(nullable = false)
    private Integer conversoes = 0;

    @Column(nullable = false)
    private Integer vendas = 0;

    @Column(nullable = false)
    private Integer falhas = 0;

    @Column(nullable = false)
    private Integer descadastros = 0;

    @Column(precision = 15, scale = 2)
    private BigDecimal receitaGerada = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal custoInvestido = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxaEntrega = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxaAbertura = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxaClique = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxaConversao = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxaDescadastro = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal roi = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal custoAquisicao = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal ticketMedio = BigDecimal.ZERO;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        calcularMetricas();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        calcularMetricas();
    }

    public void calcularMetricas() {
        // Taxa de entrega
        if (envios > 0) {
            taxaEntrega = BigDecimal.valueOf(entregues)
                .divide(BigDecimal.valueOf(envios), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // Taxa de abertura
        if (entregues > 0) {
            taxaAbertura = BigDecimal.valueOf(aberturas)
                .divide(BigDecimal.valueOf(entregues), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // Taxa de clique
        if (aberturas > 0) {
            taxaClique = BigDecimal.valueOf(cliques)
                .divide(BigDecimal.valueOf(aberturas), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // Taxa de conversão
        if (cliques > 0) {
            taxaConversao = BigDecimal.valueOf(conversoes)
                .divide(BigDecimal.valueOf(cliques), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // Taxa de descadastro
        if (entregues > 0) {
            taxaDescadastro = BigDecimal.valueOf(descadastros)
                .divide(BigDecimal.valueOf(entregues), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // ROI
        if (custoInvestido.compareTo(BigDecimal.ZERO) > 0) {
            roi = receitaGerada.subtract(custoInvestido)
                .divide(custoInvestido, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // Custo de aquisição
        if (conversoes > 0) {
            custoAquisicao = custoInvestido
                .divide(BigDecimal.valueOf(conversoes), 2, BigDecimal.ROUND_HALF_UP);
        }

        // Ticket médio
        if (vendas > 0) {
            ticketMedio = receitaGerada
                .divide(BigDecimal.valueOf(vendas), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    // Métodos para incrementar contadores
    public void incrementarEnvios() {
        this.envios++;
        calcularMetricas();
    }

    public void incrementarEntregues() {
        this.entregues++;
        calcularMetricas();
    }

    public void incrementarAberturas() {
        this.aberturas++;
        calcularMetricas();
    }

    public void incrementarCliques() {
        this.cliques++;
        calcularMetricas();
    }

    public void incrementarConversoes() {
        this.conversoes++;
        calcularMetricas();
    }

    public void incrementarVendas(BigDecimal valorVenda) {
        this.vendas++;
        this.receitaGerada = this.receitaGerada.add(valorVenda);
        calcularMetricas();
    }

    public void incrementarFalhas() {
        this.falhas++;
        calcularMetricas();
    }

    public void incrementarDescadastros() {
        this.descadastros++;
        calcularMetricas();
    }

    public void adicionarCusto(BigDecimal custo) {
        this.custoInvestido = this.custoInvestido.add(custo);
        calcularMetricas();
    }

    // Métodos de verificação de performance
    public boolean isPerformanceBoa() {
        return taxaAbertura.compareTo(BigDecimal.valueOf(20)) >= 0 && 
               taxaClique.compareTo(BigDecimal.valueOf(3)) >= 0 &&
               taxaConversao.compareTo(BigDecimal.valueOf(2)) >= 0;
    }

    public boolean isPerformanceExcelente() {
        return taxaAbertura.compareTo(BigDecimal.valueOf(30)) >= 0 && 
               taxaClique.compareTo(BigDecimal.valueOf(5)) >= 0 &&
               taxaConversao.compareTo(BigDecimal.valueOf(5)) >= 0 &&
               roi.compareTo(BigDecimal.valueOf(200)) >= 0;
    }

    public String getClassificacaoPerformance() {
        if (isPerformanceExcelente()) {
            return "Excelente";
        } else if (isPerformanceBoa()) {
            return "Boa";
        } else if (taxaAbertura.compareTo(BigDecimal.valueOf(10)) >= 0) {
            return "Regular";
        } else {
            return "Baixa";
        }
    }
}