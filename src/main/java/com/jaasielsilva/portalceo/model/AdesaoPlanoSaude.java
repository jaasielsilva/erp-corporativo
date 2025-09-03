package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "adesao_plano_saude")
public class AdesaoPlanoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_saude_id", nullable = false)
    private PlanoSaude planoSaude;

    @Column(nullable = false)
    private LocalDate dataAdesao;

    @Column
    private LocalDate dataVigenciaInicio;

    @Column
    private LocalDate dataVigenciaFim;

    @Column
    private LocalDate dataCancelamento;

    private String tipoAdesao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAdesao status = StatusAdesao.ATIVA;

    @Column(nullable = false)
    private Integer quantidadeDependentes = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensalTitular;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensalDependentes = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalMensal;

    @Column(length = 500)
    private String observacoes;

    @OneToMany(mappedBy = "adesaoPlanoSaude", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DependentePlanoSaude> dependentes;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum StatusAdesao {
        PENDENTE,
        ATIVA,
        SUSPENSA,
        CANCELADA,
        INATIVO
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusAdesao.PENDENTE;
        }
        if (quantidadeDependentes == null) {
            quantidadeDependentes = 0;
        }
        if (dataAdesao == null) {
            dataAdesao = LocalDate.now();
        }
        calcularValores();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        calcularValores();
    }

    public void calcularValores() {
        if (planoSaude != null) {
            valorMensalTitular = planoSaude.calcularValorColaboradorTitular();
            valorMensalDependentes = planoSaude.calcularValorColaboradorDependente()
                    .multiply(BigDecimal.valueOf(quantidadeDependentes));
            valorTotalMensal = valorMensalTitular.add(valorMensalDependentes);
        }
    }

    // Adicione dentro da classe AdesaoPlanoSaude
    public BigDecimal getValorTotalAtual() {
        if (planoSaude != null) {
            BigDecimal valorTitularAtual = planoSaude.calcularValorColaboradorTitular();
            BigDecimal valorDependentesAtual = planoSaude.calcularValorColaboradorDependente()
                    .multiply(BigDecimal.valueOf(quantidadeDependentes));
            return valorTitularAtual.add(valorDependentesAtual);
        }
        return BigDecimal.ZERO;
    }

    public String getStatusDescricao() {
        switch (status) {
            case PENDENTE:
                return "Pendente";
            case ATIVA:
                return "Ativa";
            case SUSPENSA:
                return "Suspensa";
            case CANCELADA:
                return "Cancelada";
            case INATIVO:
                return "Inativo";
            default:
                return status.toString();
        }
    }

    public boolean isAtiva() {
        return status == StatusAdesao.ATIVA;
    }

    public boolean isPendente() {
        return status == StatusAdesao.PENDENTE;
    }

    public boolean isCancelada() {
        return status == StatusAdesao.CANCELADA;
    }

    // Método para calcular subsídio da empresa 
    // Titular: empresa paga 80%, Dependente: empresa paga 0%
    public BigDecimal getValorSubsidioEmpresa() {
        if (planoSaude != null) {
            BigDecimal subsidioTitular = planoSaude.calcularValorEmpresaTitular();
            BigDecimal subsidioDependentes = planoSaude.calcularValorEmpresaDependente()
                    .multiply(BigDecimal.valueOf(quantidadeDependentes));
            return subsidioTitular.add(subsidioDependentes);
        }
        return BigDecimal.ZERO;
    }

    // Método para calcular desconto do colaborador
    // Titular: colaborador paga 20%, Dependente: colaborador paga 100%
    public BigDecimal getValorDesconto() {
        return valorTotalMensal;
    }
}