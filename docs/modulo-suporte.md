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

### 6. Integração e APIs
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

### Monitoramento de SLA

- **Dashboard**: Visualização em tempo real
- **Alertas**: Chamados próximos do vencimento
- **Relatórios**: Métricas de performance da equipe
- **Histórico**: Análise de tendências e melhorias

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