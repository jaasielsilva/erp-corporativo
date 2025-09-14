# Módulo de Suporte - Sistema ERP

## Visão Geral

O Módulo de Suporte é responsável pelo gerenciamento completo de chamados técnicos no sistema ERP, incluindo abertura, acompanhamento, resolução e análise de tickets de suporte com controle automático de SLA (Service Level Agreement).

## Funcionalidades Principais

### 1. Gestão de Chamados
- **Abertura de Chamados**: Criação de novos tickets com informações detalhadas
- **Acompanhamento**: Visualização do status e progresso dos chamados
- **Atribuição**: Designação de técnicos responsáveis
- **Resolução**: Marcação de chamados como resolvidos ou fechados

### 2. Sistema de SLA Automático
- **Cálculo Dinâmico**: SLA calculado automaticamente baseado na prioridade
- **Alertas Visuais**: Indicadores coloridos para status do SLA
- **Métricas**: Tempo médio de resolução e estatísticas de performance

### 3. Dashboard Analítico
- **Visão Geral**: Cards com estatísticas principais
- **Gráficos**: Distribuição por prioridade e status
- **Tabela de Chamados**: Lista dos tickets mais recentes
- **Indicadores**: SLA médio e chamados críticos

## Estrutura Técnica

### Entidades

#### Chamado.java
```java
@Entity
@Table(name = "chamados")
public class Chamado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String numero;           // Número único do chamado
    private String assunto;          // Título/assunto do chamado
    private String descricao;        // Descrição detalhada
    private Prioridade prioridade;   // Nível de prioridade
    private StatusChamado status;    // Status atual
    private LocalDateTime dataAbertura;
    private LocalDateTime dataResolucao;
    private String tecnicoResponsavel;
    private String solicitanteNome;
    private String solicitanteEmail;
    private String categoria;
    private String observacoes;
    
    @Transient
    private Long slaRestante;        // SLA restante em horas
}
```

#### Enums

**Prioridade**
- `BAIXA`: 72 horas úteis
- `MEDIA`: 48 horas úteis  
- `ALTA`: 24 horas úteis
- `URGENTE`: 8 horas úteis

**StatusChamado**
- `ABERTO`: Chamado criado, aguardando atendimento
- `EM_ANDAMENTO`: Chamado sendo atendido
- `RESOLVIDO`: Chamado resolvido, aguardando confirmação
- `FECHADO`: Chamado finalizado

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

### Contatos de Suporte
- **Desenvolvedor**: Equipe de TI
- **Administrador**: Gestor de TI
- **Usuários**: Help Desk interno

---

**Versão**: 1.0  
**Data**: Setembro 2024  
**Autor**: Sistema ERP - Módulo de Suporte  
**Status**: Implementado e Funcional