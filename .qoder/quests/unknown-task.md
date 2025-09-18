# Problema com o Cartão "Tempo de Resolução Últimos 12 meses vs Meta SLA"

## 1. Visão Geral

O cartão "Tempo de Resolução Últimos 12 meses vs Meta SLA" na página de suporte do sistema ERP não está sendo populado com dados do banco de dados. Este documento identifica as causas raiz e propõe soluções para resolver o problema.

## 2. Arquitetura do Componente

O componente utiliza uma arquitetura MVC com as seguintes camadas:

### 2.1. Fluxo de Dados

```mermaid
graph TD
    A[Frontend - index.html] --> B{Requisição AJAX<br>/suporte/api/tempo-resolucao}
    B --> C[SuporteController.getTempoResolucao()]
    C --> D[ChamadoService.obterLabelsUltimosMeses()]
    C --> E[ChamadoService.obterTempoMedioResolucaoUltimosMeses()]
    C --> F[ChamadoService.calcularMetaTempoResolucao()]
    E --> G[ChamadoRepository.findByPeriodo()]
    F --> H[Chamado.Prioridade.getHorasUteis()]
    D --> I[Processamento de dados]
    E --> I
    F --> I
    I --> J[Resposta JSON]
    J --> K[Atualização do gráfico<br>tempoResolucaoChart]
```

## 3. Problemas Identificados

### 3.1. Falha na Query do Repositório

A query `findByPeriodo()` no `ChamadoRepository` pode não estar retornando dados corretamente:

```java
@Query("SELECT c FROM Chamado c " +
       "WHERE c.dataAbertura BETWEEN :dataInicio AND :dataFim " +
       "ORDER BY c.dataAbertura DESC")
List<Chamado> findByPeriodo(@Param("dataInicio") LocalDateTime dataInicio, 
                            @Param("dataFim") LocalDateTime dataFim);
```

### 3.2. Tratamento Insuficiente de Dados Ausentes

O método `obterTempoMedioResolucaoUltimosMeses()` no `ChamadoService` não trata adequadamente cenários onde:
- Não há chamados resolvidos no período
- A query `findByPeriodo()` retorna uma lista vazia
- Há falhas na conexão com o banco de dados

### 3.3. Falha no Tratamento de Erros no Frontend

O frontend não trata adequadamente erros na requisição AJAX, resultando em falhas silenciosas.

## 4. Soluções Propostas

### 4.1. Correção no Service (ChamadoService.java)

Implementar tratamento mais robusto no método `obterTempoMedioResolucaoUltimosMeses()`:

```java
@Transactional(readOnly = true)
public List<Double> obterTempoMedioResolucaoUltimosMeses() {
    List<Double> temposResolucao = new ArrayList<>();
    LocalDate hoje = LocalDate.now();
    
    // Verificar dados históricos
    List<Chamado> todosResolvidos = chamadoRepository.findChamadosResolvidos()
        .stream()
        .filter(c -> c.getDataResolucao() != null)
        .collect(Collectors.toList());
    
    // Se não há dados históricos, retornar zeros
    if (todosResolvidos.isEmpty()) {
        for (int i = 0; i < 12; i++) {
            temposResolucao.add(0.0);
        }
        return temposResolucao;
    }
    
    // Processar dados dos últimos 12 meses
    for (int i = 11; i >= 0; i--) {
        LocalDate mesReferencia = hoje.minusMonths(i);
        LocalDateTime inicioMes = mesReferencia.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth()).atTime(23, 59, 59);
        
        try {
            List<Chamado> chamadosDoMes = chamadoRepository.findByPeriodo(inicioMes, fimMes)
                .stream()
                .filter(c -> c.getStatus() == StatusChamado.RESOLVIDO || c.getStatus() == StatusChamado.FECHADO)
                .filter(c -> c.getDataResolucao() != null)
                .collect(Collectors.toList());
            
            if (!chamadosDoMes.isEmpty()) {
                double somaHoras = chamadosDoMes.stream()
                    .mapToDouble(c -> Duration.between(c.getDataAbertura(), c.getDataResolucao()).toHours())
                    .sum();
                
                double media = somaHoras / chamadosDoMes.size();
                temposResolucao.add(Math.round(media * 100.0) / 100.0);
            } else {
                temposResolucao.add(0.0);
            }
        } catch (Exception e) {
            logger.error("Erro ao calcular tempo médio do mês {}: {}", mesReferencia, e.getMessage());
            temposResolucao.add(0.0);
        }
    }
    
    return temposResolucao;
}
```

### 4.2. Melhoria no Tratamento de Erros no Frontend

Adicionar tratamento de erros mais robusto no arquivo `index.html`:

```javascript
fetch('/suporte/api/tempo-resolucao')
    .then(response => {
        if (!response.ok) {
            throw new Error('Erro HTTP: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            // Processar dados normalmente
        } else {
            console.error('Erro nos dados:', data.error);
            atualizarMetricasTempoResolucao([], []);
        }
    })
    .catch(error => {
        console.error('Erro na requisição:', error);
        atualizarMetricasTempoResolucao([], []);
    });
```

## 5. Plano de Testes

1. Verificar se a query `findByPeriodo()` retorna dados corretos com datas válidas
2. Testar o método `obterTempoMedioResolucaoUltimosMeses()` com diferentes cenários:
   - Dados completos
   - Dados parciais
   - Nenhum dado
3. Validar o endpoint `/suporte/api/tempo-resolucao` com chamadas diretas
4. Testar o frontend com dados simulados e erros simulados

## 6. Conclusão

O problema ocorre principalmente devido à falta de tratamento adequado de dados ausentes e falhas na query do repositório. As correções propostas melhoram a robustez do sistema e garantem que o cartão seja exibido corretamente mesmo em cenários com dados incompletos.