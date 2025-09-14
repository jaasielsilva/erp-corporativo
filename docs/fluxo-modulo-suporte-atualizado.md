# Fluxo do Módulo de Suporte - Documentação Atualizada

## Visão Geral

Este documento descreve o fluxo real e atualizado do módulo de suporte do sistema ERP, baseado na análise do código fonte e correção de inconsistências identificadas.

## Fluxograma Visual

O fluxograma completo está disponível em: `fluxograma-modulo-suporte.svg`

## Fluxo Principal dos Chamados

### 1. Acesso ao Sistema
- **URL Principal**: `/suporte`
- **Controller**: `SuporteController.java`
- **Método**: `dashboard()` - Exibe a página principal com chamados recentes

### 2. Criação de Novo Chamado

#### 2.1 Formulário de Criação
- **URL**: `/suporte/novo`
- **Método**: `novoFormulario()`
- **Campos obrigatórios**:
  - Assunto
  - Descrição
  - Prioridade (URGENTE, ALTA, MEDIA, BAIXA)
  - Categoria (opcional)

#### 2.2 Processamento da Criação
- **URL**: `POST /suporte/chamados`
- **Método**: `criarChamado()`
- **Processo**:
  1. Validação dos dados
  2. Geração do número único (formato: CH + timestamp)
  3. Cálculo do SLA baseado na prioridade
  4. Status inicial: `ABERTO`
  5. Técnico: `Não atribuído`

### 3. Estados e Transições

#### 3.1 Estados Disponíveis
- **ABERTO**: Chamado criado, aguardando atendimento
- **EM_ANDAMENTO**: Técnico iniciou o atendimento
- **RESOLVIDO**: Solução implementada, aguardando confirmação
- **FECHADO**: Chamado finalizado

#### 3.2 Transições Permitidas
- ABERTO → EM_ANDAMENTO (técnico inicia atendimento)
- EM_ANDAMENTO → RESOLVIDO (técnico resolve)
- RESOLVIDO → FECHADO (cliente confirma)
- RESOLVIDO → EM_ANDAMENTO (reabertura por insatisfação)

### 4. Sistema de Prioridades e SLA

#### 4.1 Prioridades e Tempos
| Prioridade | Tempo SLA | Descrição |
|------------|-----------|----------|
| URGENTE | 8 horas | Problemas críticos que impedem operação |
| ALTA | 24 horas | Problemas importantes com impacto significativo |
| MEDIA | 48 horas | Problemas moderados |
| BAIXA | 72 horas | Melhorias e problemas menores |

#### 4.2 Cálculo do SLA
- **Horário de funcionamento**: 8h às 18h (segunda a sexta)
- **Método**: `calcularSlaRestante()` em `ChamadoService.java`
- **Considerações**:
  - Apenas horas úteis são contabilizadas
  - SLA é pausado quando status = RESOLVIDO
  - SLA é reiniciado em caso de reabertura

### 5. Interface do Usuário

#### 5.1 Dashboard Principal
- **Localização**: `/suporte`
- **Funcionalidades**:
  - Lista de chamados recentes
  - Filtros por status e prioridade
  - Indicadores visuais de SLA
  - Atualização automática a cada 30 segundos

#### 5.2 Campos Exibidos na Lista
- **Nº**: Número único do chamado
- **Assunto**: Título do problema
- **Status**: Estado atual (ABERTO, EM_ANDAMENTO, etc.)
- **Prioridade**: Nível de urgência
- **Técnico Responsável**: Nome do técnico ou "Não atribuído"
- **SLA Restante**: Tempo restante em formato legível
- **Ações**: Botões para visualizar/editar

## Exemplo Prático: Chamados do Usuário

### Chamado CH20250914153610
- **Número**: CH20250914153610
- **Assunto**: teste breve
- **Status**: ABERTO
- **Prioridade**: URGENTE
- **Técnico**: Não atribuído
- **SLA Restante**: 8h restantes
- **Fluxo esperado**:
  1. Técnico deve ser atribuído em breve (prioridade urgente)
  2. Status mudará para EM_ANDAMENTO
  3. Resolução deve ocorrer dentro de 8 horas úteis
  4. Cliente confirmará resolução (FECHADO)

### Chamado CH20250914153146
- **Número**: CH20250914153146
- **Assunto**: teste breve
- **Status**: ABERTO
- **Prioridade**: BAIXA
- **Técnico**: Não atribuído
- **SLA Restante**: 72h restantes
- **Fluxo esperado**:
  1. Atendimento pode aguardar chamados de maior prioridade
  2. Técnico será atribuído conforme disponibilidade
  3. Resolução dentro de 72 horas úteis
  4. Processo normal de fechamento

## APIs REST Disponíveis

### Endpoints Implementados
- `GET /suporte` - Dashboard principal
- `GET /suporte/novo` - Formulário de novo chamado
- `POST /suporte/chamados` - Criar novo chamado
- `GET /suporte/chamados` - Listar chamados
- `GET /suporte/chamados/{id}` - Visualizar chamado específico
- `POST /suporte/chamados/{id}/status` - Atualizar status

### Formato de Resposta JSON
```json
{
  "id": "CH20250914153610",
  "assunto": "teste breve",
  "descricao": "Descrição detalhada do problema",
  "status": "ABERTO",
  "prioridade": "URGENTE",
  "tecnicoResponsavel": null,
  "slaRestante": "8h restantes",
  "dataAbertura": "2025-09-14T15:36:10",
  "dataUltimaAtualizacao": "2025-09-14T15:36:10"
}
```

## Inconsistências Corrigidas

### 1. Duplicações Removidas
- **Problema**: Existiam `Chamado.java` e `TicketSuporte.java`
- **Solução**: Usar apenas `Chamado.java` como entidade principal

### 2. URLs Padronizadas
- **Problema**: Múltiplas páginas HTML para suporte
- **Solução**: Centralizar em `/suporte` com `SuporteController`

### 3. Enums Consistentes
- **Problema**: Diferenças entre documentação e código
- **Solução**: Documentação atualizada conforme implementação real

## Monitoramento e Alertas

### Indicadores de SLA
- **Verde**: SLA > 25% do tempo total
- **Amarelo**: SLA entre 10% e 25%
- **Vermelho**: SLA < 10% ou vencido

### Queries de Monitoramento
- `findChamadosComSlaVencido()` - Chamados com SLA vencido
- `findChamadosProximosVencimento()` - Chamados próximos ao vencimento

## Tecnologias Utilizadas

- **Backend**: Spring Boot, JPA/Hibernate
- **Frontend**: Thymeleaf, Bootstrap, JavaScript
- **Banco de Dados**: MySQL/PostgreSQL
- **Validação**: Bean Validation (JSR-303)

## Próximos Passos

1. Implementar notificações automáticas por email
2. Adicionar sistema de comentários nos chamados
3. Criar relatórios de performance de SLA
4. Implementar integração com sistema de tickets externos

---

**Última atualização**: Janeiro 2025  
**Versão**: 2.0  
**Responsável**: Equipe de Desenvolvimento