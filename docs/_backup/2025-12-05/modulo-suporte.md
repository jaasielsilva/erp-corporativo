# Módulo de Suporte - Sistema ERP

## Visão Geral

O módulo de suporte é responsável pelo gerenciamento completo de chamados técnicos e solicitações de suporte dentro do sistema ERP. Ele oferece uma solução integrada que permite aos usuários abrir chamados, acompanhar o progresso em tempo real e à equipe de suporte gerenciar eficientemente todas as demandas através de interfaces web e APIs REST.

## Funcionalidades Principais

### 1. Gestão Completa de Chamados
- **Abertura de Chamados**: Interface web intuitiva e API REST para criação de novos chamados
- **Acompanhamento em Tempo Real**: Visualização do status e progresso dos chamados
- **Categorização Avançada**: Organização por tipo, prioridade e departamento
- **Histórico Completo**: Registro detalhado de todas as interações e mudanças
- **Numeração Automática**: Sistema de numeração única para cada chamado

### 2. Sistema de Avaliações
- **Avaliação de Atendimento**: Notas de 1 a 5 estrelas para qualidade do atendimento
- **Comentários Opcionais**: Feedback detalhado dos usuários sobre a resolução
- **Métricas de Satisfação**: Cálculo automático de nota média e taxa de satisfação
- **Dashboard de Qualidade**: Estatísticas de avaliação em tempo real
- **Relatórios de Performance**: Análise de satisfação por técnico e período

### 3. Sistema de SLA (Service Level Agreement)
- **Cálculo Automático**: Definição automática de prazos baseados na prioridade
- **Alertas Inteligentes**: Notificações para chamados próximos ao vencimento
- **Métricas Avançadas**: Acompanhamento de performance e cumprimento de SLAs
- **SLA Médio**: Cálculo estatístico do tempo médio de resolução
- **Análise Temporal Profissional**: Sistema de cálculo de tempo médio de resolução por período mensal com lógica robusta similar aos ERPs TOTVS e SAP

### 4. Dashboard e Relatórios Avançados
- **Visão Geral Executiva**: Dashboard com estatísticas em tempo real
- **Gráficos Interativos**: Visualização de dados por status, prioridade e período
- **Estatísticas Detalhadas**: Contadores por status e prioridade
- **Relatórios Exportáveis**: Dados estruturados para análise externa
- **Métricas de Avaliação**: Indicadores de satisfação e qualidade do atendimento

### 5. Gestão de Status e Workflow
- **Workflow Estruturado**: Fluxo bem definido de status dos chamados
- **Atribuição de Técnicos**: Designação e controle de responsáveis
- **Atualizações Controladas**: Registro sistemático de progresso e soluções
- **APIs de Atualização**: Endpoints REST para mudanças de status

### 6. Backlog de Chamados (Sistema de Fila Inteligente)
- **Priorização Automática**: Algoritmo inteligente que calcula score de prioridade baseado em múltiplos fatores
- **Gestão de Fila**: Posicionamento automático dos chamados na fila de atendimento
- **Categorização Inteligente**: Classificação automática por urgência, complexidade e impacto no negócio
- **Sugestão de Técnico**: Recomendação automática do técnico mais adequado baseado na expertise
- **Estimativa de Atendimento**: Cálculo automático do tempo estimado para atendimento
- **Recálculo Dinâmico**: Atualização automática das prioridades a cada 5 minutos
- **Métricas Avançadas**: Estatísticas detalhadas de performance e distribuição da fila
- **APIs Especializadas**: Endpoints dedicados para gestão do backlog

## Estrutura Técnica

### Entidades

#### Estrutura da Entidade Chamado

```java
@Entity
@Data
@Table(name = "chamados")
public class Chamado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero", unique = true, nullable = false)
    private String numero;           // Número único do chamado
    
    @NotBlank(message = "Assunto é obrigatório")
    @Size(min = 5, max = 200, message = "Assunto deve ter entre 5 e 200 caracteres")
    @Column(name = "assunto", nullable = false)
    private String assunto;          // Título/assunto do chamado
    
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, message = "Descrição deve ter pelo menos 10 caracteres")
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;        // Descrição detalhada
    
    @NotNull(message = "Prioridade é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false)
    private Prioridade prioridade;   // Nível de prioridade
    
    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusChamado status = StatusChamado.ABERTO;    // Status atual
    
    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;
    
    @Column(name = "data_inicio_atendimento")
    private LocalDateTime dataInicioAtendimento;
    
    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;
    
    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;
    
    @Column(name = "sla_vencimento")
    private LocalDateTime slaVencimento;
    
    @Column(name = "tecnico_responsavel")
    private String tecnicoResponsavel;
    
    @Column(name = "solicitante_nome")
    private String solicitanteNome;
    
    @Column(name = "solicitante_email")
    private String solicitanteEmail;
    
    @Column(name = "categoria")
    private String categoria;
    
    @Column(name = "subcategoria")
    private String subcategoria;
    
    @Column(name = "tempo_resolucao_minutos")
    private Integer tempoResolucaoMinutos;
    
    // Campos de avaliação
    @Column(name = "avaliacao")
    private Integer avaliacao;       // Nota de 1 a 5 estrelas
    
    @Column(name = "comentario_avaliacao", columnDefinition = "TEXT")
    private String comentarioAvaliacao; // Comentário opcional do usuário
    
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
    
    private LocalDateTime dataInicio; // Campo adicional para controle
    
    // Campo transiente para SLA restante (calculado dinamicamente)
    @Transient
    private Long slaRestante;        // SLA restante em horas
}
```
