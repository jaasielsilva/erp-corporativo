# Documentação de UI e Navegação - Módulo Financeiro

## Visão Geral
O Módulo Financeiro do ERP Corporativo foi padronizado para oferecer uma experiência de usuário (UX) consistente, eficiente e profissional. Todos os templates seguem um sistema de design unificado, definido principalmente no arquivo `financeiro.css`.

## Estrutura de Arquivos
- **CSS Principal**: `/css/financeiro.css`
- **Templates**: `src/main/resources/templates/financeiro/`
  - `index.html`: Dashboard principal
  - `contas-pagar/lista.html`: Gestão de contas a pagar
  - `contas-receber/lista.html`: Gestão de contas a receber
  - `fluxo-caixa.html`: Visão consolidada de entradas e saídas
  - `transferencias.html`: Gestão de transferências internas
  - `relatorios.html`: Geração de relatórios gerenciais

## Componentes de UI Padronizados

### 1. Navegação do Módulo
Uma barra de navegação secundária permite acesso rápido às principais áreas do módulo sem a necessidade de retornar ao menu lateral principal.

```html
<div class="module-nav">
    <a href="/financeiro" class="module-nav-link active">
        <i class="fas fa-chart-line"></i> Dashboard
    </a>
    <a href="/financeiro/contas-pagar" class="module-nav-link">
        <i class="fas fa-file-invoice-dollar"></i> Contas a Pagar
    </a>
    <!-- Outros links... -->
</div>
```

### 2. Cartões de KPI (Key Performance Indicators)
Utilizados para exibir métricas importantes de forma destacada no topo das páginas.

- **Classe Base**: `.kpi-card`
- **Variações de Cor**:
  - `.kpi-primary`: Azul (Neutro/Informativo)
  - `.kpi-success`: Verde (Positivo/Receitas)
  - `.kpi-danger`: Vermelho (Negativo/Despesas/Vencidos)
  - `.kpi-warning`: Amarelo (Alerta/Pendente)

```html
<div class="kpi-card kpi-success">
    <div class="card-body">
        <i class="fas fa-arrow-up kpi-icon"></i>
        <div class="kpi-title">Entradas</div>
        <div class="kpi-value">R$ 10.000,00</div>
    </div>
</div>
```

### 3. Barra de Filtros
Uma área dedicada para filtros de pesquisa, padronizada com fundo claro e espaçamento adequado.

- **Classe**: `.filter-bar`
- **Elementos**: Labels com texto mudo e negrito (`text-muted small fw-bold`), inputs e selects do Bootstrap, botão de ação primário.

```html
<div class="filter-bar">
    <form class="row g-3 align-items-end">
        <div class="col-md-3">
            <label class="form-label text-muted small fw-bold">Período</label>
            <input type="date" class="form-control">
        </div>
        <div class="col-md-2">
            <button type="submit" class="btn btn-primary w-100">Filtrar</button>
        </div>
    </form>
</div>
```

### 4. Tabelas de Dados
Tabelas limpas e modernas para listagem de registros.

- **Container**: `.financeiro-table` (adiciona bordas arredondadas e sombra)
- **Tabela**: `.table table-hover align-middle mb-0`
- **Badges de Status**:
  - `.badge-status`: Classe base
  - `.badge-pago` / `.badge-recebido`: Verde
  - `.badge-pendente`: Amarelo
  - `.badge-vencida`: Vermelho

```html
<div class="financeiro-table table-responsive">
    <table class="table table-hover align-middle mb-0">
        <thead>
            <tr>
                <th>Descrição</th>
                <th>Status</th>
                <th class="text-end">Valor</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Serviço X</td>
                <td><span class="badge-status badge-pago">PAGO</span></td>
                <td class="text-end">R$ 100,00</td>
            </tr>
        </tbody>
    </table>
</div>
```

## Diretrizes de Design

1.  **Cores**: Utilize as variáveis CSS do Bootstrap, complementadas pelas definições específicas em `financeiro.css` para tons de fundo e bordas suaves.
2.  **Ícones**: Use FontAwesome 6 para todos os ícones. Ícones de KPI devem ter a classe `.kpi-icon`.
3.  **Feedback Visual**: Sempre forneça feedback para estados vazios (ex: "Nenhuma transação encontrada") usando ícones desbotados e mensagens amigáveis.
4.  **Consistência**: Mantenha a ordem dos elementos: Título > Navegação do Módulo > KPIs > Filtros > Tabela/Conteúdo.

## Próximos Passos
- Implementar paginação AJAX para as tabelas.
- Adicionar gráficos interativos (Chart.js) no Dashboard e Relatórios.
- Melhorar a responsividade para dispositivos móveis (ajustes finos em tabelas e KPIs).
