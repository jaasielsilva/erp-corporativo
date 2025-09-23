# 📊 DOCUMENTAÇÃO COMPLETA - PÁGINA DE SUPORTE
## Sistema ERP Corporativo - Dashboard de Suporte

**URL**: `http://localhost:8080/suporte`  
**Versão**: 2.0.0  
**Data**: Janeiro 2025  
**Status**: ✅ **TOTALMENTE FUNCIONAL**

---

## 📖 Índice

1. [Visão Geral](#visão-geral)
2. [Cards de Estatísticas](#cards-de-estatísticas)
3. [Gráficos Principais](#gráficos-principais)
4. [Gráficos Secundários](#gráficos-secundários)
5. [Métricas de Performance](#métricas-de-performance)
6. [Métricas SLA](#métricas-sla)
7. [Tabelas e Relatórios](#tabelas-e-relatórios)
8. [Endpoints e APIs](#endpoints-e-apis)
9. [Como Testar](#como-testar)

---

## 🎯 Visão Geral

A página de suporte é um dashboard completo que oferece uma visão 360° do sistema de atendimento, incluindo métricas em tempo real, análises históricas e indicadores de performance. Todos os dados são atualizados dinamicamente através de APIs REST.

### 🏗️ Estrutura da Página

```
📊 Dashboard de Suporte
├── 📈 Cards de Estatísticas (6 cards principais)
├── 📊 Gráficos Principais (2 gráficos grandes)
├── 📉 Gráficos Secundários (3 gráficos médios)
├── ⏱️ Métricas de Performance (4 cards de tempo)
├── 🎯 Métricas SLA (4 cards + 3 gráficos)
├── 📋 Tabelas de Dados
└── 🔧 Ações Rápidas
```

---

## 📈 Cards de Estatísticas

### 1. 🟢 **Chamados Abertos**
- **Localização**: Primeira linha, primeiro card
- **Função**: Exibe o número total de chamados com status "ABERTO"
- **Endpoint**: Dados calculados em tempo real via JavaScript
- **Atualização**: Automática a cada carregamento da página
- **Indicador**: Número em destaque + ícone de ticket
- **Cor**: Verde (#28a745)

```javascript
// Cálculo realizado no frontend
const chamadosAbertos = dadosChamados.filter(c => c.status === 'ABERTO').length;
```

### 2. 🟡 **Em Andamento**
- **Localização**: Primeira linha, segundo card
- **Função**: Mostra chamados sendo atendidos atualmente
- **Status**: "EM_ANDAMENTO"
- **Indicador**: Número + ícone de engrenagem
- **Cor**: Amarelo (#ffc107)
- **Importância**: Indica capacidade de atendimento atual

### 3. 🔵 **Resolvidos**
- **Localização**: Primeira linha, terceiro card
- **Função**: Total de chamados resolvidos (status "RESOLVIDO")
- **Indicador**: Número + ícone de check
- **Cor**: Azul (#007bff)
- **Métrica**: Produtividade da equipe

### 4. ⏱️ **SLA Médio**
- **Localização**: Segunda linha, primeiro card
- **Função**: Tempo médio de resolução em horas
- **Endpoint**: `/suporte/api/tempo-resolucao`
- **Cálculo**: Média dos tempos de resolução dos últimos 30 dias
- **Formato**: "XX.X horas"
- **Cor**: Roxo (#6f42c1)

### 5. ⭐ **Avaliação Média**
- **Localização**: Segunda linha, segundo card
- **Função**: Nota média das avaliações de atendimento
- **Endpoint**: `/suporte/api/avaliacoes-atendimento`
- **Escala**: 1 a 5 estrelas
- **Formato**: "X.X ⭐"
- **Cor**: Dourado (#fd7e14)

### 6. 🚀 **Ações Rápidas**
- **Localização**: Segunda linha, terceiro card
- **Função**: Botões de ação para operações comuns
- **Ações**:
  - 📝 **Novo Chamado**: Redireciona para `/suporte/novo`
  - 📋 **Ver Todos**: Redireciona para `/suporte/chamados`
- **Cor**: Cinza (#6c757d)

---

## 📊 Gráficos Principais

### 1. 📈 **Evolução de Chamados**
- **Localização**: Seção principal, lado esquerdo
- **Tipo**: Gráfico de linha (Line Chart)
- **Endpoint**: `/suporte/api/evolucao-chamados`
- **Período**: Últimos 6 meses
- **Dados**: Número de chamados abertos por mês
- **Canvas ID**: `evolucaoChart`

```javascript
// Configuração do gráfico
new Chart(ctx, {
    type: 'line',
    data: {
        labels: ['Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
        datasets: [{
            label: 'Chamados Abertos',
            data: [12, 19, 15, 25, 22, 18],
            borderColor: '#007bff',
            backgroundColor: 'rgba(0, 123, 255, 0.1)'
        }]
    }
});
```

### 2. 🍰 **Chamados por Categoria**
- **Localização**: Seção principal, lado direito
- **Tipo**: Gráfico de pizza (Doughnut Chart)
- **Endpoint**: Dados calculados no frontend
- **Categorias**:
  - 🔧 **Técnico**: Hardware, Software, Rede, E-mail, Site
  - 💰 **Financeiro**: Pagamentos, Recebimentos, Relatórios
  - 👥 **RH**: Acesso, Dados, Treinamento, Benefícios
  - 💼 **Vendas**: Pedidos, Clientes, Comissões, Promoções
  - 📦 **Estoque**: Movimentação, Inventário, Produtos
- **Canvas ID**: `categoriasChart`

---

## 📉 Gráficos Secundários

### 1. 🎯 **Distribuição por Prioridade**
- **Localização**: Segunda seção, primeiro gráfico
- **Tipo**: Gráfico de barras horizontais
- **Endpoint**: `/suporte/api/prioridades`
- **Prioridades**:
  - 🔴 **CRÍTICA**: Problemas que impedem operação
  - 🟠 **ALTA**: Problemas importantes com impacto significativo
  - 🟡 **MÉDIA**: Problemas moderados
  - 🟢 **BAIXA**: Problemas menores ou dúvidas
- **Canvas ID**: `prioridadesChart`

### 2. ⏰ **Tempo de Resolução**
- **Localização**: Segunda seção, segundo gráfico
- **Tipo**: Gráfico de barras
- **Endpoint**: `/suporte/api/tempo-resolucao`
- **Dados**: Tempo médio por categoria
- **Unidade**: Horas
- **Canvas ID**: `tempoResolucaoChart`

### 3. 📊 **Avaliações de Atendimento**
- **Localização**: Segunda seção, terceiro gráfico
- **Tipo**: Gráfico de barras
- **Endpoint**: `/suporte/api/avaliacoes-atendimento`
- **Escala**: 1 a 5 estrelas
- **Dados**: Distribuição das notas
- **Canvas ID**: `avaliacoesChart`

---

## ⏱️ Métricas de Performance

### 1. 📊 **Tempo Médio de Resolução - Últimos 30 dias**
- **Localização**: Seção de métricas, primeiro card
- **Endpoint**: `/suporte/api/tempo-medio-ultimos-dias?dias=30`
- **Função**: Tempo médio para resolver chamados
- **Formato**: "XX.X horas"
- **Indicador**: Tendência (↑↓) comparado ao período anterior
- **Implementação**: ✅ **RECÉM IMPLEMENTADO**

```json
// Resposta da API
{
    "success": true,
    "tempoMedio": 18.5,
    "tendencia": "baixa",
    "variacao": -2.3,
    "periodo": "30 dias"
}
```

### 2. 📈 **Métricas de Resolução**
- **Localização**: Seção de métricas, segundo card
- **Endpoint**: `/suporte/api/metricas-resolucao`
- **Dados**:
  - Total de chamados resolvidos
  - Tempo médio de resolução
  - Taxa de resolução no primeiro contato
  - Chamados reabertos

### 3. 🎯 **Taxa de Resolução no Prazo**
- **Localização**: Seção de métricas, terceiro card
- **Cálculo**: (Chamados resolvidos no prazo / Total resolvidos) × 100
- **Meta**: 85% ou superior
- **Indicador**: Percentual + status visual

### 4. 📞 **Primeiro Contato**
- **Localização**: Seção de métricas, quarto card
- **Função**: Percentual de chamados resolvidos no primeiro atendimento
- **Meta**: 70% ou superior
- **Importância**: Eficiência do atendimento

---

## 🎯 Métricas SLA

### Cards SLA

#### 1. 📊 **Cumprimento de SLA**
- **Endpoint**: `/suporte/api/metricas-sla-periodo?dias=30`
- **Cálculo**: (Chamados dentro do SLA / Total de chamados) × 100
- **Meta**: 95% ou superior
- **Cores**:
  - 🟢 Verde: ≥ 95%
  - 🟡 Amarelo: 85-94%
  - 🔴 Vermelho: < 85%

#### 2. ⏱️ **SLA Médio**
- **Função**: Tempo médio de SLA dos chamados
- **Unidade**: Horas
- **Cálculo**: Média ponderada por prioridade

#### 3. ✅ **Chamados no Prazo**
- **Função**: Número absoluto de chamados resolvidos dentro do SLA
- **Indicador**: Número + percentual

#### 4. ❌ **Chamados Fora do Prazo**
- **Função**: Número de chamados que estouraram o SLA
- **Indicador**: Número + percentual
- **Ação**: Requer atenção especial

### Gráficos SLA

#### 1. 📈 **Tendência de Cumprimento SLA**
- **Canvas ID**: `slaCumprimentoChart`
- **Tipo**: Gráfico de linha
- **Período**: Últimos 30 dias
- **Endpoint**: `/suporte/api/tendencia-sla?dias=30`

#### 2. ⏰ **Tempo Médio de Resolução**
- **Canvas ID**: `slaTempoMedioChart`
- **Tipo**: Gráfico de barras
- **Comparação**: Por período

#### 3. 🎯 **Distribuição por Status SLA**
- **Canvas ID**: `slaDistribuicaoChart`
- **Tipo**: Gráfico de pizza
- **Categorias**: No prazo, Fora do prazo, Em risco

---

## 📋 Tabelas e Relatórios

### 1. 📊 **Tabela de Comparativo de Períodos**
- **Localização**: Seção SLA
- **Dados**:
  - Período atual vs anterior
  - Variação percentual
  - Tendência

### 2. 📋 **Lista de Chamados**
- **Localização**: Parte inferior da página
- **Dados**: Últimos chamados criados
- **Colunas**:
  - Número do chamado
  - Assunto
  - Status
  - Prioridade
  - Data de abertura
  - SLA restante

---

## 🔌 Endpoints e APIs

### APIs de Dados para Cards
```
GET /suporte/api/evolucao-chamados          # Evolução mensal
GET /suporte/api/prioridades                # Distribuição por prioridade
GET /suporte/api/status                     # Distribuição por status
GET /suporte/api/tempo-resolucao            # Tempo médio por categoria
GET /suporte/api/avaliacoes-atendimento     # Notas e satisfação
```

### APIs de Métricas
```
GET /suporte/api/metricas-resolucao         # Métricas gerais
GET /suporte/api/tempo-medio-ultimos-dias   # Tempo médio configurável
GET /suporte/api/metricas-sla-periodo       # SLA por período
GET /suporte/api/metricas-sla-comparativo   # Comparação de períodos
GET /suporte/api/tendencia-sla              # Tendência temporal
```

### APIs de Operações
```
GET /suporte/api/categorias                 # Categorias disponíveis
GET /suporte/api/backlog                    # Dados do backlog
GET /suporte/api/backlog/proximo            # Próximo chamado
POST /api/chamados                          # Criar novo chamado
PUT /api/chamados/{id}/status               # Atualizar status
```

---

## 🧪 Como Testar

### 1. **Preparar Dados de Teste**

```sql
-- Execute o script SQL
mysql -u root -p erp_corporativo < script-teste-suporte.sql
```

### 2. **Executar Script JavaScript**

```javascript
// No console do navegador (F12)
// Carregar o script
fetch('/script-criar-chamado-teste.js')
  .then(response => response.text())
  .then(script => eval(script));

// Executar testes completos
testeSuporte.executarTestesCompletos();

// Ou criar chamado rápido
testeSuporte.criarChamadoRapido({
    assunto: 'Teste de Demonstração',
    categoria: 'TECNICO',
    prioridade: 'ALTA'
});
```

### 3. **Verificar Resultados**

1. **Acesse**: `http://localhost:8080/suporte`
2. **Faça login** no sistema
3. **Observe**:
   - ✅ Todos os cards devem mostrar números
   - ✅ Gráficos devem estar populados
   - ✅ Métricas SLA devem exibir percentuais
   - ✅ Tabelas devem listar chamados

### 4. **Casos de Uso para Demonstração**

#### 🎯 **Caso de Uso 1: Gestor de TI**
- **Objetivo**: Monitorar performance da equipe
- **Foco**: Cards de tempo, SLA e gráficos de tendência
- **Ações**: Verificar cumprimento de metas, identificar gargalos

#### 👥 **Caso de Uso 2: Analista de Suporte**
- **Objetivo**: Acompanhar fila de atendimento
- **Foco**: Chamados abertos, em andamento, backlog
- **Ações**: Priorizar atendimentos, distribuir carga

#### 📊 **Caso de Uso 3: Diretor de Operações**
- **Objetivo**: Visão estratégica do suporte
- **Foco**: Métricas de satisfação, evolução histórica
- **Ações**: Tomar decisões sobre recursos e processos

---

## 🎨 Personalização e Configuração

### Períodos Configuráveis
- **7 dias**: Visão semanal
- **30 dias**: Visão mensal (padrão)
- **60 dias**: Visão bimestral
- **90 dias**: Visão trimestral

### Cores e Temas
```css
:root {
    --cor-critica: #dc3545;    /* Vermelho */
    --cor-alta: #fd7e14;       /* Laranja */
    --cor-media: #ffc107;      /* Amarelo */
    --cor-baixa: #28a745;      /* Verde */
    --cor-primaria: #007bff;   /* Azul */
}
```

### Metas e Thresholds
```javascript
const METAS = {
    sla_cumprimento: 95,        // 95% de cumprimento
    tempo_resolucao: 24,        // 24 horas máximo
    satisfacao: 4.0,            // 4 estrelas mínimo
    primeiro_contato: 70        // 70% no primeiro contato
};
```

---

## 🔧 Troubleshooting

### Problemas Comuns

#### 1. **Cards não carregam dados**
- ✅ Verificar se o servidor está rodando
- ✅ Verificar logs do console (F12)
- ✅ Testar endpoints individualmente

#### 2. **Gráficos não aparecem**
- ✅ Verificar se Chart.js está carregado
- ✅ Verificar se os canvas existem no DOM
- ✅ Verificar dados retornados pelas APIs

#### 3. **Métricas SLA zeradas**
- ✅ Verificar se existem chamados com SLA definido
- ✅ Executar script de dados de teste
- ✅ Verificar cálculos no backend

---

## 📈 Próximas Melhorias

### Funcionalidades Planejadas
- 🔄 **Atualização em tempo real** via WebSocket
- 📱 **Versão mobile** responsiva
- 📧 **Alertas por e-mail** para SLA em risco
- 📊 **Dashboards personalizáveis** por usuário
- 🤖 **IA para predição** de tempo de resolução

### Integrações Futuras
- 📞 **Sistema de telefonia** (CTI)
- 💬 **Chat em tempo real**
- 📱 **App mobile** nativo
- 🔗 **Integração com ITSM** externos

---

**✅ Documentação completa e atualizada**  
**🌐 Acesse: http://localhost:8080/suporte**  
**📧 Suporte: suporte@empresa.com**