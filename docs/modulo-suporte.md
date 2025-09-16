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

### 7. Integração e APIs
- **API REST Completa**: Endpoints para todas as operações CRUD
- **Autenticação Integrada**: Sistema de login e controle de acesso
- **Formato JSON**: Comunicação padronizada via JSON
- **Tratamento de Erros**: Respostas estruturadas com códigos HTTP apropriados

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

#### Estrutura da Entidade BacklogChamado

```java
@Entity
@Data
@Table(name = "backlog_chamados")
public class BacklogChamado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "chamado_id", nullable = false, unique = true)
    private Chamado chamado;                    // Chamado associado
    
    @Column(name = "data_entrada_backlog", nullable = false)
    private LocalDateTime dataEntradaBacklog;   // Quando entrou no backlog
    
    @Column(name = "posicao_fila")
    private Integer posicaoFila;                // Posição atual na fila
    
    @Column(name = "score_prioridade")
    private Double scorePrioridade;             // Score calculado de prioridade
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_urgencia")
    private CategoriaUrgencia categoriaUrgencia; // Categoria de urgência
    
    @Enumerated(EnumType.STRING)
    @Column(name = "complexidade_estimada")
    private ComplexidadeEstimada complexidadeEstimada; // Complexidade estimada
    
    @Enumerated(EnumType.STRING)
    @Column(name = "impacto_negocio")
    private ImpactoNegocio impactoNegocio;      // Impacto no negócio
    
    @Column(name = "sla_critico")
    private Boolean slaCritico = false;         // Se tem SLA crítico
    
    @Column(name = "cliente_vip")
    private Boolean clienteVip = false;         // Se é cliente VIP
    
    @Column(name = "tempo_espera_minutos")
    private Long tempoEsperaMinutos;            // Tempo de espera em minutos
    
    @Column(name = "tecnico_sugerido")
    private String tecnicoSugerido;             // Técnico sugerido
    
    @Column(name = "estimativa_atendimento")
    private LocalDateTime estimativaAtendimento; // Estimativa de quando será atendido
    
    // Enums internos
    public enum CategoriaUrgencia {
        BAIXA("Baixa", 10.0),
        MEDIA("Média", 30.0),
        ALTA("Alta", 60.0),
        CRITICA("Crítica", 100.0);
        
        private final String descricao;
        private final Double peso;
    }
    
    public enum ComplexidadeEstimada {
        BAIXA("Baixa", 5.0),
        MEDIA("Média", 15.0),
        ALTA("Alta", 30.0),
        CRITICA("Crítica", 50.0);
        
        private final String descricao;
        private final Double peso;
    }
    
    public enum ImpactoNegocio {
        BAIXO("Baixo", 5.0),
        MEDIO("Médio", 20.0),
        ALTO("Alto", 40.0),
        CRITICO("Crítico", 80.0);
        
        private final String descricao;
        private final Double peso;
    }
    
    // Métodos de cálculo automático
    public void calcularScore() {
        double score = 0.0;
        
        // Peso da urgência (30%)
        if (categoriaUrgencia != null) {
            score += categoriaUrgencia.getPeso() * 0.30;
        }
        
        // Peso da complexidade (20%)
        if (complexidadeEstimada != null) {
            score += complexidadeEstimada.getPeso() * 0.20;
        }
        
        // Peso do impacto (25%)
        if (impactoNegocio != null) {
            score += impactoNegocio.getPeso() * 0.25;
        }
        
        // Bônus SLA crítico (15%)
        if (Boolean.TRUE.equals(slaCritico)) {
            score += 15.0;
        }
        
        // Bônus cliente VIP (10%)
        if (Boolean.TRUE.equals(clienteVip)) {
            score += 10.0;
        }
        
        // Fator tempo de espera
        if (tempoEsperaMinutos != null && tempoEsperaMinutos > 0) {
            double fatorTempo = Math.min(tempoEsperaMinutos / 60.0, 24.0); // Máximo 24h
            score += fatorTempo * 0.5; // 0.5 pontos por hora
        }
        
        this.scorePrioridade = Math.min(score, 100.0); // Máximo 100
    }
    
    public void atualizarTempoEspera() {
        if (dataEntradaBacklog != null) {
            this.tempoEsperaMinutos = ChronoUnit.MINUTES.between(
                dataEntradaBacklog, LocalDateTime.now()
            );
        }
    }
}
```

#### Enums

**Prioridade**
- `BAIXA("Baixa", 72)`: 72 horas úteis
- `MEDIA("Média", 48)`: 48 horas úteis  
- `ALTA("Alta", 24)`: 24 horas úteis
- `URGENTE("Urgente", 8)`: 8 horas úteis

**StatusChamado**
- `ABERTO("Aberto")`: Chamado criado, aguardando atendimento
- `EM_ANDAMENTO("Em Andamento")`: Chamado sendo atendido
- `RESOLVIDO("Resolvido")`: Chamado resolvido, aguardando confirmação
- `FECHADO("Fechado")`: Chamado finalizado

### Serviços

#### ChamadoService.java

**Métodos Principais:**

```java
// Cálculo de SLA restante baseado na prioridade
public Long calcularSlaRestante(Chamado chamado)

// Cálculo do tempo médio de resolução
public Double calcularSlaMedio()

// Operações CRUD
public Chamado criarChamado(Chamado chamado)
public Optional<Chamado> buscarPorId(Long id)
public List<Chamado> listarTodos()

// Mudanças de status
public Chamado iniciarAtendimento(Long id, String tecnico)
public Chamado resolverChamado(Long id)
public Chamado fecharChamado(Long id)

// Consultas especializadas
public List<Chamado> buscarChamadosComSlaVencido()
public List<Chamado> buscarChamadosComSlaProximoVencimento()
```

### Repositório

#### ChamadoRepository.java

**Queries Principais:**

```java
// Estatísticas para dashboard
@Query("SELECT AVG(TIMESTAMPDIFF(HOUR, c.dataAbertura, c.dataResolucao)) FROM Chamado c WHERE c.status IN ('RESOLVIDO', 'FECHADO')")
Double calcularTempoMedioResolucaoEmHoras();

// Chamados com SLA crítico
@Query("SELECT c FROM Chamado c WHERE c.status IN ('ABERTO', 'EM_ANDAMENTO') AND ...")
List<Chamado> findChamadosComSlaVencido();

// Agrupamentos para gráficos
@Query("SELECT c.prioridade, COUNT(c) FROM Chamado c GROUP BY c.prioridade")
List<Object[]> countByPrioridadeGrouped();
```

#### BacklogChamadoService.java

**Métodos Principais:**

```java
// Gestão da fila de backlog
public void adicionarAoBacklog(Chamado chamado);
public void removerDoBacklog(Long chamadoId);
public BacklogChamado obterProximoChamado();
public List<BacklogChamado> obterFilaCompleta();

// Cálculos de priorização
public void recalcularPrioridades();
public void atualizarPosicoesFila();
public Double calcularScorePrioridade(BacklogChamado backlog);

// Categorização automática
public CategoriaUrgencia determinarUrgencia(Chamado chamado);
public ComplexidadeEstimada estimarComplexidade(Chamado chamado);
public ImpactoNegocio avaliarImpacto(Chamado chamado);

// Sugestão de técnico
public String sugerirTecnico(BacklogChamado backlog);
public List<String> obterTecnicosDisponiveis();

// Estimativas de tempo
public LocalDateTime calcularEstimativaAtendimento(BacklogChamado backlog);
public Long calcularTempoEsperaEstimado(Integer posicaoFila);

// Métricas e relatórios
public Map<String, Object> obterEstatisticasBacklog();
public Map<String, Object> obterMetricasPerformance();
public List<BacklogChamado> obterChamadosComSlaVencendo();
```

#### BacklogChamadoRepository.java

**Queries Especializadas:**

```java
// Ordenação por prioridade
@Query("SELECT b FROM BacklogChamado b ORDER BY b.scorePrioridade DESC, b.dataEntradaBacklog ASC")
List<BacklogChamado> findAllOrderedByPriority();

// Próximo chamado da fila
@Query("SELECT b FROM BacklogChamado b WHERE b.posicaoFila = 1")
Optional<BacklogChamado> findNextInQueue();

// Estatísticas do backlog
@Query("SELECT COUNT(b), AVG(b.tempoEsperaMinutos), MAX(b.scorePrioridade) FROM BacklogChamado b")
Object[] getBacklogStatistics();

// Chamados por categoria de urgência
@Query("SELECT b.categoriaUrgencia, COUNT(b) FROM BacklogChamado b GROUP BY b.categoriaUrgencia")
List<Object[]> countByUrgenciaCategory();

// Tempo médio de espera por prioridade
@Query("SELECT c.prioridade, AVG(b.tempoEsperaMinutos) FROM BacklogChamado b JOIN b.chamado c GROUP BY c.prioridade")
List<Object[]> averageWaitTimeByPriority();

// Chamados com SLA crítico no backlog
@Query("SELECT b FROM BacklogChamado b WHERE b.slaCritico = true ORDER BY b.scorePrioridade DESC")
List<BacklogChamado> findCriticalSlaInBacklog();
```

### Controller

#### SuporteController.java

**Endpoints:**

- `GET /suporte` - Dashboard principal
- `GET /suporte/chamados` - Lista de chamados
- `GET /suporte/chamados/{id}` - Visualizar chamado específico
- `GET /suporte/novo` - Formulário de novo chamado
- `POST /suporte/novo` - Criar novo chamado
- `POST /suporte/chamados/{id}/status` - Atualizar status
- `GET /suporte/api/prioridades` - Dados para gráfico de prioridades
- `GET /suporte/api/status` - Dados para gráfico de status

**Endpoints do Backlog:**

- `GET /suporte/backlog` - Interface do backlog de chamados
- `GET /suporte/api/backlog/fila` - Lista completa da fila do backlog
- `GET /suporte/api/backlog/dados` - Dados e estatísticas do backlog
- `GET /suporte/api/backlog/proximo` - Próximo chamado da fila
- `POST /suporte/api/backlog/adicionar/{chamadoId}` - Adicionar chamado ao backlog
- `DELETE /suporte/api/backlog/remover/{chamadoId}` - Remover chamado do backlog
- `POST /suporte/api/backlog/recalcular` - Recalcular prioridades da fila

## Regras de SLA

### Definição de Horas Úteis
- **Dias**: Segunda a Sexta-feira
- **Horário**: 8h às 18h
- **Feriados**: Não contabilizados

### Cálculo do SLA
1. **Início**: Data/hora de abertura do chamado
2. **Prazo**: Baseado na prioridade (em horas úteis)
3. **Contagem**: Apenas horas úteis são consideradas
4. **Status**: Calculado dinamicamente a cada consulta

### Indicadores Visuais
- 🟢 **Verde**: SLA dentro do prazo (> 4h restantes)
- 🟡 **Amarelo**: SLA próximo do vencimento (≤ 4h restantes)
- 🔴 **Vermelho**: SLA vencido (≤ 0h)

## Interface do Usuário

### Dashboard Principal
- **Cards de Resumo**: Chamados abertos, em andamento, resolvidos e SLA médio
- **Gráficos**: Distribuição por prioridade e status
- **Tabela**: Últimos 10 chamados com indicadores de SLA
- **Botão Flutuante**: Acesso rápido para abertura de chamados

### Funcionalidades da Interface
- **Atualização Automática**: Dados atualizados em tempo real
- **Filtros**: Por status, prioridade, técnico
- **Ações Rápidas**: Visualizar, editar, alterar status
- **Responsividade**: Interface adaptável para diferentes dispositivos

### Interface do Backlog

#### Dashboard do Backlog
- **Métricas em Tempo Real**: Total de chamados na fila, tempo médio de espera, score máximo
- **Gráficos Especializados**: 
  - Distribuição por categoria de urgência
  - Tempo médio de espera por prioridade
  - Evolução do backlog ao longo do tempo
- **Indicadores Visuais**: Cores baseadas no score de prioridade e SLA crítico

#### Fila de Chamados
- **Ordenação Inteligente**: Baseada no score de prioridade calculado automaticamente
- **Informações Detalhadas**:
  - Posição na fila
  - Score de prioridade (0-100)
  - Categoria de urgência
  - Complexidade estimada
  - Impacto no negócio
  - Tempo de espera atual
  - Técnico sugerido
  - Estimativa de atendimento
- **Ações Disponíveis**:
  - Visualizar detalhes do chamado
  - Remover da fila
  - Recalcular prioridades
  - Atender próximo chamado

#### Funcionalidades Avançadas
- **Categorização Automática**: Sistema inteligente que analisa o conteúdo e classifica automaticamente
- **Sugestão de Técnico**: Baseada em especialização, disponibilidade e histórico
- **Estimativas Dinâmicas**: Cálculo em tempo real do tempo estimado de atendimento
- **Alertas de SLA**: Notificações visuais para chamados com SLA crítico
- **Recálculo Automático**: Atualização periódica dos scores baseada no tempo de espera

## Algoritmos e Regras de Negócio do Backlog

### Cálculo do Score de Prioridade

O sistema utiliza um algoritmo ponderado que considera múltiplos fatores:

```
Score = (Urgência × 0.30) + (Complexidade × 0.20) + (Impacto × 0.25) + 
        Bônus_SLA_Crítico + Bônus_Cliente_VIP + Fator_Tempo_Espera

Onde:
- Urgência: 10-100 pontos (Baixa=10, Média=30, Alta=60, Crítica=100)
- Complexidade: 5-50 pontos (Baixa=5, Média=15, Alta=30, Crítica=50)
- Impacto: 5-80 pontos (Baixo=5, Médio=20, Alto=40, Crítico=80)
- Bônus SLA Crítico: +15 pontos
- Bônus Cliente VIP: +10 pontos
- Fator Tempo: +0.5 pontos por hora de espera (máximo 24h)
```

### Regras de Categorização Automática

#### Categoria de Urgência
- **Crítica**: Palavras-chave como "sistema fora do ar", "não consigo trabalhar"
- **Alta**: "lento", "erro frequente", "impacta produtividade"
- **Média**: "dúvida", "configuração", "melhoria"
- **Baixa**: "sugestão", "quando possível", "não urgente"

#### Complexidade Estimada
- **Crítica**: Problemas de infraestrutura, integrações complexas
- **Alta**: Desenvolvimento de novas funcionalidades
- **Média**: Configurações avançadas, correções de bugs
- **Baixa**: Dúvidas, orientações, configurações simples

#### Impacto no Negócio
- **Crítico**: Afeta múltiplos usuários ou processos críticos
- **Alto**: Afeta departamento inteiro ou processo importante
- **Médio**: Afeta equipe específica
- **Baixo**: Afeta usuário individual

### Algoritmo de Sugestão de Técnico

1. **Análise de Especialização**: Baseada no histórico de chamados resolvidos
2. **Verificação de Disponibilidade**: Considera carga atual de trabalho
3. **Matching de Competências**: Relaciona tipo de problema com expertise
4. **Balanceamento de Carga**: Distribui chamados equitativamente

### Estimativa de Tempo de Atendimento

```
Tempo_Estimado = (Posição_Fila - 1) × Tempo_Médio_Atendimento + 
                 Complexidade_Estimada_Minutos

Onde:
- Tempo_Médio_Atendimento: Calculado dinamicamente baseado no histórico
- Complexidade_Estimada_Minutos: Baixa=30, Média=90, Alta=180, Crítica=360
```

## Tecnologias Utilizadas

- **Backend**: Spring Boot, JPA/Hibernate
- **Frontend**: Thymeleaf, Bootstrap 5, Chart.js
- **Banco de Dados**: MySQL (queries otimizadas)
- **Validação**: Bean Validation (JSR-303)
- **Logging**: SLF4J + Logback

## Configuração e Instalação

### 1. Banco de Dados
```sql
-- A tabela será criada automaticamente pelo JPA
-- Configurar no application.properties:
spring.jpa.hibernate.ddl-auto=update
```

### 2. Dependências Maven
```xml
<!-- Já incluídas no projeto principal -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 3. Configurações
```properties
# Configurações específicas do módulo (se necessário)
suporte.sla.horario-inicio=8
suporte.sla.horario-fim=18
suporte.dashboard.limite-chamados=10

# Configurações do Backlog
suporte.backlog.recalculo-automatico=true
suporte.backlog.intervalo-recalculo-minutos=15
suporte.backlog.tempo-medio-atendimento-minutos=120
suporte.backlog.max-score=100
suporte.backlog.peso-urgencia=0.30
suporte.backlog.peso-complexidade=0.20
suporte.backlog.peso-impacto=0.25
suporte.backlog.bonus-sla-critico=15
suporte.backlog.bonus-cliente-vip=10
suporte.backlog.fator-tempo-por-hora=0.5
suporte.backlog.max-horas-fator-tempo=24
```

## Uso e Operação

### Fluxo Típico de um Chamado

1. **Abertura**
   - Usuário acessa `/suporte/novo`
   - Preenche formulário com assunto, descrição e prioridade
   - Sistema gera número único e calcula SLA

2. **Atendimento**
   - Técnico visualiza chamado no dashboard
   - Inicia atendimento (status: EM_ANDAMENTO)
   - Adiciona observações conforme necessário

3. **Resolução**
   - Técnico marca como RESOLVIDO
   - Sistema registra data/hora de resolução
   - SLA é recalculado para estatísticas

4. **Fechamento**
   - Solicitante confirma resolução
   - Chamado é marcado como FECHADO
   - Métricas são atualizadas

### Fluxo do Backlog de Chamados

1. **Entrada no Backlog**
   - Chamado é automaticamente adicionado ao backlog quando criado
   - Sistema executa categorização automática
   - Calcula score inicial de prioridade
   - Define posição na fila baseada no score

2. **Priorização Inteligente**
   - Sistema analisa conteúdo e determina urgência
   - Estima complexidade baseada em palavras-chave
   - Avalia impacto no negócio
   - Aplica bônus para SLA crítico e clientes VIP

3. **Gestão da Fila**
   - Recálculo automático a cada 15 minutos
   - Atualização do tempo de espera
   - Reordenação baseada em novos scores
   - Sugestão de técnico mais adequado

4. **Atendimento**
   - Técnico acessa próximo chamado da fila
   - Chamado é removido automaticamente do backlog
   - Métricas de performance são atualizadas
   - Estimativas são recalculadas para fila restante

### Monitoramento de SLA

- **Dashboard**: Visualização em tempo real
- **Alertas**: Chamados próximos do vencimento
- **Relatórios**: Métricas de performance da equipe
- **Histórico**: Análise de tendências e melhorias

### Métricas e Relatórios do Backlog

#### Métricas em Tempo Real
- **Total de Chamados na Fila**: Quantidade atual no backlog
- **Tempo Médio de Espera**: Média de tempo que chamados aguardam atendimento
- **Score Máximo**: Maior score de prioridade na fila atual
- **Distribuição por Urgência**: Percentual de chamados por categoria
- **Chamados com SLA Crítico**: Quantidade de chamados com risco de vencimento

#### Relatórios de Performance
- **Eficiência da Fila**: Tempo médio entre entrada e atendimento
- **Precisão da Categorização**: Taxa de acerto da classificação automática
- **Balanceamento de Carga**: Distribuição de chamados por técnico
- **Evolução do Backlog**: Histórico de crescimento/redução da fila
- **Impacto do Score**: Correlação entre score e tempo real de resolução

#### Dashboards Especializados
- **Gráfico de Distribuição por Urgência**: Pizza com categorias de urgência
- **Linha do Tempo do Backlog**: Evolução da fila ao longo do dia/semana
- **Heatmap de Atendimento**: Horários de maior demanda
- **Ranking de Técnicos**: Performance baseada em chamados do backlog
- **Análise de Tendências**: Previsão de crescimento da fila

#### Alertas Inteligentes
- **Fila Crítica**: Quando backlog excede limite configurado
- **SLA em Risco**: Chamados próximos do vencimento na fila
- **Desbalanceamento**: Quando um técnico tem carga excessiva
- **Score Anômalo**: Chamados com score muito alto que precisam atenção
- **Tempo Excessivo**: Chamados há muito tempo na fila

## Manutenção e Evolução

### Logs e Monitoramento
- Logs detalhados de todas as operações
- Métricas de performance do sistema
- Alertas para falhas ou problemas

### Possíveis Melhorias Futuras
- **Notificações**: Email/SMS para alertas de SLA
- **Integração**: APIs para sistemas externos
- **Relatórios**: Dashboards avançados com BI
- **Automação**: Regras de atribuição automática
- **Chat**: Sistema de comunicação em tempo real
- **Base de Conhecimento**: FAQ e documentação
- **Satisfação**: Pesquisas de satisfação do cliente

#### Melhorias Específicas do Backlog
- **Machine Learning**: Algoritmos de aprendizado para melhor categorização
- **Previsão de Demanda**: Análise preditiva para dimensionamento da equipe
- **Otimização Dinâmica**: Ajuste automático dos pesos do algoritmo de score
- **Integração com Calendário**: Consideração de feriados e horários especiais
- **Análise de Sentimento**: Detecção de urgência baseada no tom da mensagem
- **Gamificação**: Sistema de pontuação para técnicos baseado na eficiência
- **API de Terceiros**: Integração com ferramentas de monitoramento externas
- **Workflow Avançado**: Regras complexas de roteamento baseadas em contexto

## Casos de Uso

### Caso de Uso 1: Abertura de Chamado pelo Usuário

**Ator Principal**: Usuário do Sistema
**Objetivo**: Registrar uma solicitação de suporte

**Fluxo Principal**:
1. Usuário acessa o sistema ERP
2. Navega para o módulo de Suporte (`/suporte`)
3. Clica em "Novo Chamado" ou acessa diretamente `/suporte/novo`
4. Preenche o formulário com:
    - Assunto (obrigatório)
    - Descrição detalhada (obrigatório)
    - Prioridade (BAIXA, MEDIA, ALTA, CRITICA)
    - Email para contato (opcional)
5. Submete o formulário
6. Sistema gera número único do chamado (formato: CHM-YYYYMMDDHHMMSS)
7. Sistema calcula SLA baseado na prioridade
8. Chamado é criado com status "ABERTO"
9. Usuário é redirecionado para visualização do chamado

**Fluxos Alternativos**:
- **3a**: Usuário pode criar chamado via API REST (`POST /api/chamados`)
- **5a**: Se dados inválidos, sistema exibe erros de validação
- **5b**: Se erro no servidor, usuário é redirecionado com mensagem de erro

### Caso de Uso 2: Atendimento de Chamado pelo Técnico

**Ator Principal**: Técnico de Suporte
**Objetivo**: Processar e resolver chamado

**Fluxo Principal**:
1. Técnico acessa dashboard de suporte (`/suporte`)
2. Visualiza chamados abertos na tabela
3. Seleciona chamado para atendimento
4. Clica em "Iniciar Atendimento"
5. Sistema atualiza status para "EM_ANDAMENTO"
6. Sistema registra técnico responsável
7. Técnico trabalha na resolução
8. Ao finalizar, clica em "Resolver Chamado"
9. Sistema atualiza status para "RESOLVIDO"
10. Sistema registra data/hora da resolução

**Fluxos Alternativos**:
- **4a**: Técnico pode atualizar status via API (`PUT /api/chamados/{id}/status`)
- **8a**: Técnico pode adicionar observações antes de resolver
- **10a**: Cliente pode reabrir chamado se não satisfeito

### Caso de Uso 3: Monitoramento de SLA

**Ator Principal**: Gestor de Suporte
**Objetivo**: Acompanhar performance e cumprimento de SLAs

**Fluxo Principal**:
1. Gestor acessa dashboard principal (`/suporte`)
2. Visualiza estatísticas em tempo real:
   - Total de chamados abertos
   - Chamados em andamento
   - Chamados resolvidos
   - SLA médio
3. Analisa gráficos de distribuição por prioridade e status
4. Verifica chamados com SLA crítico
5. Identifica gargalos e oportunidades de melhoria

**Fluxos Alternativos**:
- **2a**: Gestor pode acessar estatísticas via API (`GET /api/chamados/estatisticas`)
- **4a**: Sistema pode enviar alertas automáticos para SLAs próximos do vencimento

### Caso de Uso 4: Gestão do Backlog de Chamados

**Ator Principal**: Técnico de Suporte
**Objetivo**: Atender chamados de forma otimizada usando o sistema de fila inteligente

**Fluxo Principal**:
1. Técnico acessa interface do backlog (`/suporte/backlog`)
2. Visualiza fila ordenada por prioridade inteligente
3. Analisa informações do próximo chamado:
   - Score de prioridade
   - Categoria de urgência
   - Complexidade estimada
   - Tempo de espera
   - Técnico sugerido
4. Clica em "Atender Próximo Chamado"
5. Sistema remove chamado do backlog automaticamente
6. Técnico inicia atendimento do chamado
7. Sistema recalcula posições da fila restante

**Fluxos Alternativos**:
- **3a**: Técnico pode visualizar detalhes completos do chamado antes de aceitar
- **4a**: Técnico pode pular chamado se não for de sua especialidade
- **7a**: Sistema pode sugerir técnico alternativo baseado em disponibilidade

### Caso de Uso 5: Monitoramento e Análise do Backlog

**Ator Principal**: Gestor de Suporte
**Objetivo**: Monitorar eficiência da fila e otimizar processos

**Fluxo Principal**:
1. Gestor acessa dashboard do backlog (`/suporte/backlog`)
2. Analisa métricas em tempo real:
   - Total de chamados na fila
   - Tempo médio de espera
   - Distribuição por urgência
   - Score máximo atual
3. Visualiza gráficos especializados:
   - Evolução do backlog ao longo do tempo
   - Heatmap de atendimento por horário
   - Ranking de performance dos técnicos
4. Identifica gargalos e oportunidades:
   - Chamados com tempo excessivo na fila
   - Desbalanceamento de carga entre técnicos
   - Padrões de demanda por categoria
5. Toma ações corretivas:
   - Recalcula prioridades manualmente
   - Redistribui chamados entre técnicos
   - Ajusta configurações do algoritmo

**Fluxos Alternativos**:
- **2a**: Gestor pode acessar dados via API (`GET /api/backlog/dados`)
- **5a**: Sistema pode executar recálculo automático baseado em configuração
- **5b**: Alertas automáticos podem ser enviados quando limites são ultrapassados

### Caso de Uso 6: Categorização Automática de Chamados

**Ator Principal**: Sistema (Processo Automático)
**Objetivo**: Classificar automaticamente novos chamados no backlog

**Fluxo Principal**:
1. Novo chamado é criado no sistema
2. Sistema analisa conteúdo (assunto + descrição)
3. Executa algoritmos de categorização:
   - Determina categoria de urgência baseada em palavras-chave
   - Estima complexidade baseada no tipo de problema
   - Avalia impacto no negócio baseado no contexto
4. Aplica bônus especiais:
   - Verifica se é SLA crítico
   - Identifica se é cliente VIP
5. Calcula score de prioridade usando algoritmo ponderado
6. Sugere técnico mais adequado baseado em:
   - Especialização no tipo de problema
   - Disponibilidade atual
   - Histórico de performance
7. Insere chamado na posição correta da fila
8. Calcula estimativa de tempo de atendimento

**Fluxos Alternativos**:
- **3a**: Se categorização automática falhar, usa valores padrão
- **6a**: Se nenhum técnico especializado disponível, usa balanceamento de carga
- **7a**: Se fila estiver vazia, chamado vai para primeira posição

## Resumo das Funcionalidades do Backlog

### Principais Benefícios

✅ **Priorização Inteligente**: Algoritmo avançado que considera múltiplos fatores para ordenar chamados

✅ **Categorização Automática**: Sistema que analisa conteúdo e classifica automaticamente urgência, complexidade e impacto

✅ **Sugestão de Técnico**: Matching inteligente baseado em especialização e disponibilidade

✅ **Estimativas Precisas**: Cálculo dinâmico de tempo de atendimento baseado na posição na fila

✅ **Métricas Avançadas**: Dashboard especializado com indicadores de performance da fila

✅ **Recálculo Automático**: Atualização periódica das prioridades baseada no tempo de espera

✅ **Interface Intuitiva**: Visualização clara da fila com todas as informações relevantes

✅ **APIs RESTful**: Endpoints especializados para integração e automação

### Impacto na Operação

- **Redução do Tempo de Resposta**: Priorização inteligente garante que chamados críticos sejam atendidos primeiro
- **Melhoria na Satisfação**: Estimativas precisas e comunicação proativa com usuários
- **Otimização de Recursos**: Distribuição equilibrada de chamados entre técnicos
- **Visibilidade Gerencial**: Métricas detalhadas para tomada de decisão
- **Automação de Processos**: Redução de tarefas manuais de triagem e classificação
- **Escalabilidade**: Sistema preparado para crescimento da demanda

### Tecnologias e Arquitetura

- **Entidade Dedicada**: `BacklogChamado` com relacionamento 1:1 com `Chamado`
- **Service Especializado**: `BacklogChamadoService` com lógica de negócio complexa
- **Repository Otimizado**: Queries especializadas para performance
- **Controller RESTful**: Endpoints dedicados para operações do backlog
- **Algoritmos Configuráveis**: Pesos e parâmetros ajustáveis via properties
- **Cálculos em Tempo Real**: Atualização dinâmica de scores e posições

## Fluxograma do Sistema

```
┌─────────────────┐
│   USUÁRIO       │
│   ACESSA        │
│   SISTEMA       │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   DASHBOARD     │
│   SUPORTE       │
│   /suporte      │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐      ┌─────────────────┐
│   NOVO CHAMADO  │◄────►│   LISTAR        │
│   /suporte/novo │      │   CHAMADOS      │
└─────────┬───────┘      │   /suporte/     │
          │              │   chamados      │
          ▼              └─────────────────┘
┌─────────────────┐
│   PREENCHER     │
│   FORMULÁRIO    │
│   - Assunto     │
│   - Descrição   │
│   - Prioridade  │
│   - Email       │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   VALIDAÇÃO     │
│   DADOS         │
└─────────┬───────┘
          │
      ┌───▼───┐
      │ VÁLIDO│
      │   ?   │
      └───┬───┘
          │
    ┌─────▼─────┐
    │    SIM    │
    └─────┬─────┘
          │
          ▼
┌─────────────────┐
│   CRIAR         │
│   CHAMADO       │
│   - Gerar Nº    │
│   - Calcular    │
│     SLA         │
│   - Status:     │
│     ABERTO      │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   SALVAR NO     │
│   BANCO DE      │
│   DADOS         │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   VISUALIZAR    │
│   CHAMADO       │
│   CRIADO        │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   TÉCNICO       │
│   VISUALIZA     │
│   CHAMADOS      │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   INICIAR       │
│   ATENDIMENTO   │
│   Status:       │
│   EM_ANDAMENTO  │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   TRABALHAR     │
│   NA SOLUÇÃO    │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   RESOLVER      │
│   CHAMADO       │
│   Status:       │
│   RESOLVIDO     │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   FECHAR        │
│   CHAMADO       │
│   Status:       │
│   FECHADO       │
└─────────────────┘
```

## Estados e Transições

### Estados do Chamado

1. **ABERTO**: Chamado criado, aguardando atendimento
2. **EM_ANDAMENTO**: Técnico iniciou o atendimento
3. **RESOLVIDO**: Solução implementada, aguardando confirmação
4. **FECHADO**: Chamado finalizado definitivamente

### Transições Permitidas

```
 ABERTO → EM_ANDAMENTO (Técnico inicia atendimento)
 EM_ANDAMENTO → RESOLVIDO (Técnico resolve)
 RESOLVIDO → FECHADO (Confirmação da resolução)
 RESOLVIDO → EM_ANDAMENTO (Reabertura por insatisfação)
 EM_ANDAMENTO → ABERTO (Retorno para fila)
 ```

### Cálculo de SLA

**Prioridades e Tempos**:
 - **CRITICA**: 2 horas
 - **ALTA**: 4 horas  
 - **MEDIA**: 8 horas
 - **BAIXA**: 24 horas
 
 **Fórmula**: `SLA Restante = Prazo SLA - (Tempo Atual - Data Criação)`

## APIs REST Disponíveis

### Endpoints de Consulta
- `GET /api/chamados` - Lista todos os chamados
- `GET /api/chamados/{id}` - Busca chamado por ID
- `GET /api/chamados/numero/{numero}` - Busca por número
- `GET /api/chamados/abertos` - Lista chamados abertos
- `GET /api/chamados/em-andamento` - Lista chamados em andamento
- `GET /api/chamados/resolvidos` - Lista chamados resolvidos
- `GET /api/chamados/estatisticas` - Estatísticas gerais
- `GET /api/avaliacoes-atendimento` - Métricas de avaliação dos chamados
- `GET /suporte/api/evolucao-chamados` - Dados para gráfico de evolução de chamados
- `GET /suporte/chamados/{id}/json` - Busca dados de um chamado em formato JSON
- `GET /suporte/test` - Endpoint de teste para verificar funcionamento do servidor
- `POST /api/chamados/{id}/avaliacao` - Permite avaliar um chamado resolvido ou fechado

### Endpoints de Modificação
- `POST /api/chamados` - Cria novo chamado
- `PUT /api/chamados/{id}/status` - Atualiza status
- `POST /suporte/chamados/{id}/status` - Atualiza status de um chamado específico via web

### Endpoints Web
- `GET /suporte` - Dashboard principal
- `GET /suporte/chamados` - Lista de chamados
- `GET /suporte/novo` - Formulário de novo chamado
- `POST /suporte/chamados/novo` - Processa criação de chamado
- `GET /suporte/chamados/{id}` - Visualiza chamado específico
- `GET /suporte/chamados/{id}/avaliar` - Página para avaliar chamado resolvido
- `GET /suporte/chamados/{id}/debug` - Página de debug para desenvolvimento
- `GET /suporte/teste-chamado` - Página de teste para novo chamado
- `GET /suporte/teste-simples` - Template simplificado de teste
- `GET /suporte/novo-simples` - Formulário simplificado de novo chamado
- `POST /suporte/chamados/{id}/status` - Atualiza status via web

## Troubleshooting

### Problemas Comuns

1. **SLA não calculado corretamente**
   - Verificar configuração de horário comercial
   - Validar cálculo de horas úteis
   - Conferir timezone do servidor

2. **Dashboard não carrega dados**
   - Verificar conexão com banco de dados
   - Validar queries do repository
   - Conferir logs de erro

3. **Performance lenta**
   - Otimizar queries com índices
   - Implementar cache para consultas frequentes
   - Revisar paginação de resultados

4. **API retorna erro 500**
   - Verificar autenticação
   - Confirmar formato JSON da requisição
   - Verificar logs do controller

### Logs Importantes

```bash
# Verificar logs do módulo
tail -f logs/application.log | grep "ChamadoService\|SuporteController"

# Verificar erros de banco
tail -f logs/application.log | grep "ERROR.*Chamado"

# Verificar requisições API
tail -f logs/application.log | grep "API"
```

### Comandos Úteis

```sql
-- Verificar chamados no banco
SELECT * FROM chamados ORDER BY data_abertura DESC LIMIT 10;

-- Estatísticas rápidas
SELECT status, COUNT(*) FROM chamados GROUP BY status;

-- Chamados com SLA vencido
SELECT * FROM chamados WHERE sla_restante < 0;

-- Chamados por prioridade
SELECT prioridade, COUNT(*) FROM chamados GROUP BY prioridade;
```

### Testes de API

```bash
# Testar criação de chamado
curl -X POST http://localhost:8080/api/chamados \
  -H "Content-Type: application/json" \
  -d '{"assunto":"Teste API","descricao":"Teste via curl","prioridade":"MEDIA"}'

# Listar chamados
curl http://localhost:8080/api/chamados

# Buscar chamado específico
curl http://localhost:8080/api/chamados/1

# Atualizar status
curl -X PUT http://localhost:8080/api/chamados/1/status?status=EM_ANDAMENTO&tecnicoResponsavel=João
```

### Contatos de Suporte
- **Desenvolvedor**: Equipe de TI
- **Administrador**: Gestor de TI
- **Usuários**: Help Desk interno

---

**Versão**: 1.0  
**Data**: Setembro 2024  
**Autor**: Sistema ERP - Módulo de Suporte  
**Status**: Implementado e Funcional