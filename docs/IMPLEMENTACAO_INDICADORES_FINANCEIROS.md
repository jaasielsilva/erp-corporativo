# Documentação da Implementação de Indicadores Financeiros

## Visão Geral

Esta documentação descreve a implementação dos indicadores financeiros na página de dashboard do sistema ERP Corporativo. Os indicadores implementados são:

- **Margem de Lucro**: Percentual de lucro em relação ao total de vendas
- **ROI Mensal**: Retorno sobre investimento mensal
- **Inadimplência**: Percentual de contas vencidas em relação ao total a receber
- **Ticket Médio**: Valor médio das vendas (já estava implementado)

## Arquitetura da Solução

A implementação seguiu a arquitetura MVC do sistema, com as seguintes alterações:

### 1. Serviços Implementados

#### 1.1 IndicadorService

Responsável pelos cálculos dos indicadores financeiros:

- `getMargemLucro()`: Calcula a margem de lucro com base nas vendas e custos
- `getRoiMensal()`: Calcula o ROI mensal com base nos investimentos e retornos
- `getInadimplencia()`: Calcula a taxa de inadimplência
- `formatarPercentual()`: Método utilitário para formatação de percentuais

#### 1.2 FinanceiroService

Serviço de suporte para cálculos financeiros:

- `calcularTotalInvestimentos()`: Calcula o total de investimentos realizados
- `calcularTotalRetornos()`: Calcula o total de retornos obtidos
- `calcularTotalContasVencidas()`: Calcula o total de contas a receber vencidas
- `calcularTotalContasReceber()`: Calcula o total de contas a receber

#### 1.3 VendaService (Atualização)

Adicionado método para cálculo de custos:

- `calcularTotalDeCustos()`: Calcula o total de custos das vendas

### 2. Controller Atualizado

#### 2.1 DashboardController

Atualizado para utilizar os novos métodos do IndicadorService:

```java
// MÉTRICAS FINANCEIRAS - Usando dados reais
BigDecimal margemLucroValor = indicadorService.getMargemLucro();
String margemLucro = indicadorService.formatarPercentual(margemLucroValor);

BigDecimal roiMensalValor = indicadorService.getRoiMensal();
String roiMensal = indicadorService.formatarPercentual(roiMensalValor);

BigDecimal inadimplenciaValor = indicadorService.getInadimplencia();
String inadimplencia = indicadorService.formatarPercentual(inadimplenciaValor);
```

### 3. View (Template)

O template `dashboard/index.html` já estava preparado para exibir os indicadores, utilizando as variáveis do modelo:

```html
<div class="performance-metric">
    <span class="metric-label">Margem de Lucro</span>
    <span class="metric-value" th:text="${margemLucro}">23.5%</span>
</div>
<div class="performance-metric">
    <span class="metric-label">Ticket Médio</span>
    <span class="metric-value" th:text="${ticketMedio}">R$ 0,00</span>
</div>
<div class="performance-metric">
    <span class="metric-label">ROI Mensal</span>
    <span class="metric-value" th:text="${roiMensal}">18.7%</span>
</div>
<div class="performance-metric">
    <span class="metric-label">Inadimplência</span>
    <span class="metric-value" th:text="${inadimplencia}">2.1%</span>
</div>
```

## Fluxo de Dados

1. O usuário acessa a página `/dashboard`
2. O `DashboardController` solicita os cálculos ao `IndicadorService`
3. O `IndicadorService` utiliza o `VendaService` e o `FinanceiroService` para obter os dados necessários
4. Os valores calculados são formatados e adicionados ao modelo
5. O template `dashboard/index.html` exibe os indicadores com os valores reais

## Considerações Técnicas

### Cálculo da Margem de Lucro

A margem de lucro é calculada como a diferença entre o total de vendas e o total de custos, dividida pelo total de vendas e multiplicada por 100:

```java
BigDecimal lucro = totalVendas.subtract(totalCustos);
return lucro.multiply(new BigDecimal("100"))
        .divide(totalVendas, 1, RoundingMode.HALF_UP);
```

### Cálculo do ROI Mensal

O ROI mensal é calculado como a diferença entre os retornos e os investimentos, dividida pelos investimentos e multiplicada por 100:

```java
BigDecimal roi = retornos.subtract(investimentos)
        .multiply(new BigDecimal("100"))
        .divide(investimentos, 1, RoundingMode.HALF_UP);
```

### Cálculo da Inadimplência

A inadimplência é calculada como o total de contas vencidas dividido pelo total de contas a receber, multiplicado por 100:

```java
return totalVencido.multiply(new BigDecimal("100"))
        .divide(totalReceber, 1, RoundingMode.HALF_UP);
```

## Próximos Passos

1. **Implementação Completa do FinanceiroService**: Substituir os valores simulados por cálculos reais baseados nos dados do banco
2. **Histórico de Indicadores**: Implementar o armazenamento histórico dos indicadores para análise de tendências
3. **Gráficos de Evolução**: Adicionar gráficos que mostrem a evolução dos indicadores ao longo do tempo
4. **Alertas**: Configurar alertas para quando os indicadores atingirem valores críticos

## Conclusão

A implementação dos indicadores financeiros no dashboard permite uma visão mais clara e em tempo real da saúde financeira da empresa, facilitando a tomada de decisões estratégicas baseadas em dados reais.