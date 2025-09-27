package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidade que representa um item no backlog de chamados
 * Gerencia a fila de chamados aguardando atendimento com priorização inteligente
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "backlog_chamados")
public class BacklogChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chamado_id", nullable = false, unique = true)
    private Chamado chamado;

    @Column(name = "posicao_fila", nullable = false)
    private Integer posicaoFila;

    @Column(name = "score_prioridade", nullable = false)
    private Double scorePrioridade;

    @Column(name = "tempo_espera_minutos")
    private Long tempoEsperaMinutos;

    @Column(name = "data_entrada_backlog", nullable = false)
    private LocalDateTime dataEntradaBacklog;

    @Column(name = "estimativa_atendimento")
    private LocalDateTime estimativaAtendimento;

    @Column(name = "categoria_urgencia")
    @Enumerated(EnumType.STRING)
    private CategoriaUrgencia categoriaUrgencia;

    @Column(name = "tecnico_sugerido")
    private String tecnicoSugerido;

    @Column(name = "complexidade_estimada")
    @Enumerated(EnumType.STRING)
    private ComplexidadeEstimada complexidadeEstimada;

    @Column(name = "tags_contexto")
    private String tagsContexto; // JSON ou string separada por vírgulas

    @Column(name = "sla_critico")
    private Boolean slaCritico = false;

    @Column(name = "cliente_vip")
    private Boolean clienteVip = false;

    @Column(name = "impacto_negocio")
    @Enumerated(EnumType.STRING)
    private ImpactoNegocio impactoNegocio;

    // Construtor para criação automática
    public BacklogChamado(Chamado chamado) {
        this.chamado = chamado;
        this.dataEntradaBacklog = LocalDateTime.now();
        this.calcularScorePrioridade();
        this.determinarCategoriaUrgencia();
        this.estimarComplexidade();
        this.calcularImpactoNegocio();
    }

    // Métodos de negócio
    public void calcularScorePrioridade() {
        double score = 0.0;
        
        // Base da prioridade do chamado (peso 40%)
        switch (chamado.getPrioridade()) {
            case URGENTE -> score += 40.0;
            case ALTA -> score += 30.0;
            case MEDIA -> score += 20.0;
            case BAIXA -> score += 10.0;
        }
        
        // Tempo de espera (peso 25%)
        long minutosEspera = ChronoUnit.MINUTES.between(dataEntradaBacklog, LocalDateTime.now());
        score += Math.min(25.0, minutosEspera / 60.0 * 5); // Máximo 25 pontos após 5 horas
        
        // SLA crítico (peso 20%)
        if (slaCritico != null && slaCritico) {
            score += 20.0;
        }
        
        // Cliente VIP (peso 10%)
        if (clienteVip != null && clienteVip) {
            score += 10.0;
        }
        
        // Impacto no negócio (peso 5%)
        if (impactoNegocio != null) {
            switch (impactoNegocio) {
                case CRITICO -> score += 5.0;
                case ALTO -> score += 3.0;
                case MEDIO -> score += 1.0;
                case BAIXO -> score += 0.0;
            }
        }
        
        this.scorePrioridade = Math.round(score * 100.0) / 100.0;
    }

    public void determinarCategoriaUrgencia() {
        if (slaCritico != null && slaCritico) {
            this.categoriaUrgencia = CategoriaUrgencia.SLA_CRITICO;
        } else if (chamado.getPrioridade() == Chamado.Prioridade.URGENTE) {
            this.categoriaUrgencia = CategoriaUrgencia.URGENTE;
        } else if (clienteVip != null && clienteVip) {
            this.categoriaUrgencia = CategoriaUrgencia.CLIENTE_VIP;
        } else {
            this.categoriaUrgencia = CategoriaUrgencia.NORMAL;
        }
    }

    public void estimarComplexidade() {
        // Lógica simples baseada na descrição e categoria
        String descricao = chamado.getDescricao().toLowerCase();
        
        if (descricao.contains("sistema fora") || descricao.contains("não funciona") || 
            descricao.contains("erro crítico") || descricao.contains("urgente")) {
            this.complexidadeEstimada = ComplexidadeEstimada.ALTA;
        } else if (descricao.contains("lento") || descricao.contains("problema") || 
                   descricao.contains("erro")) {
            this.complexidadeEstimada = ComplexidadeEstimada.MEDIA;
        } else {
            this.complexidadeEstimada = ComplexidadeEstimada.BAIXA;
        }
    }

    public void calcularImpactoNegocio() {
        // Determina impacto baseado na prioridade e contexto
        if (chamado.getPrioridade() == Chamado.Prioridade.URGENTE && 
            (clienteVip != null && clienteVip)) {
            this.impactoNegocio = ImpactoNegocio.CRITICO;
        } else if (chamado.getPrioridade() == Chamado.Prioridade.ALTA) {
            this.impactoNegocio = ImpactoNegocio.ALTO;
        } else if (chamado.getPrioridade() == Chamado.Prioridade.MEDIA) {
            this.impactoNegocio = ImpactoNegocio.MEDIO;
        } else {
            this.impactoNegocio = ImpactoNegocio.BAIXO;
        }
    }

    public void atualizarTempoEspera() {
        this.tempoEsperaMinutos = ChronoUnit.MINUTES.between(dataEntradaBacklog, LocalDateTime.now());
    }

    public void calcularEstimativaAtendimento(int posicaoAtual, double tempoMedioAtendimento) {
        // Estima quando o chamado será atendido baseado na posição na fila
        long minutosEstimados = (long) (posicaoAtual * tempoMedioAtendimento);
        this.estimativaAtendimento = LocalDateTime.now().plusMinutes(minutosEstimados);
    }

    // Enums
    public enum CategoriaUrgencia {
        SLA_CRITICO("SLA Crítico", 1),
        URGENTE("Urgente", 2),
        CLIENTE_VIP("Cliente VIP", 3),
        NORMAL("Normal", 4);

        private final String descricao;
        private final int ordem;

        CategoriaUrgencia(String descricao, int ordem) {
            this.descricao = descricao;
            this.ordem = ordem;
        }

        public String getDescricao() { return descricao; }
        public int getOrdem() { return ordem; }
    }

    public enum ComplexidadeEstimada {
        BAIXA("Baixa", 30),
        MEDIA("Média", 60),
        ALTA("Alta", 120);

        private final String descricao;
        private final int minutosEstimados;

        ComplexidadeEstimada(String descricao, int minutosEstimados) {
            this.descricao = descricao;
            this.minutosEstimados = minutosEstimados;
        }

        public String getDescricao() { return descricao; }
        public int getMinutosEstimados() { return minutosEstimados; }
    }

    public enum ImpactoNegocio {
        CRITICO("Crítico", 4),
        ALTO("Alto", 3),
        MEDIO("Médio", 2),
        BAIXO("Baixo", 1);

        private final String descricao;
        private final int peso;

        ImpactoNegocio(String descricao, int peso) {
            this.descricao = descricao;
            this.peso = peso;
        }

        public String getDescricao() { return descricao; }
        public int getPeso() { return peso; }
    }

    @Override
    public String toString() {
        return "BacklogChamado{" +
                "id=" + id +
                ", chamado=" + (chamado != null ? chamado.getNumero() : "null") +
                ", posicaoFila=" + posicaoFila +
                ", scorePrioridade=" + scorePrioridade +
                ", categoriaUrgencia=" + categoriaUrgencia +
                '}';
    }
}