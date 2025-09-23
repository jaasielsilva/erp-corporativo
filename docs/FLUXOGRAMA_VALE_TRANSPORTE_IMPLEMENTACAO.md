# Fluxograma - Implementação Vale Transporte

## Visão Geral da Arquitetura

```mermaid
graph TB
    subgraph "Camada Apresentação"
        UI[Templates Thymeleaf]
        MODAL[Modal Cadastro]
        LISTA[Lista Colaboradores]
        RESUMO[Resumo Estatísticas]
    end
    
    subgraph "Camada Controle"
        VTC[ValeTransporteController]
        ENDPOINTS[Endpoints REST]
    end
    
    subgraph "Camada Negócio"
        VTS[ValeTransporteService]
        CS[ColaboradorService]
        CALC[Cálculos Automáticos]
    end
    
    subgraph "Camada Dados"
        VTR[ValeTransporteRepository]
        CR[ColaboradorRepository]
        DB[(Database)]
    end
    
    UI --> VTC
    MODAL --> VTC
    VTC --> VTS
    VTC --> CS
    VTS --> VTR
    VTS --> CALC
    VTR --> DB
    CR --> DB
```

## Fluxo Principal - Gestão Vale Transporte

```mermaid
flowchart TD
    START([Início]) --> LOGIN{Usuário Logado?}
    LOGIN -->|Não| AUTH[Fazer Login]
    AUTH --> MENU
    LOGIN -->|Sim| MENU[Menu Vale Transporte]
    
    MENU --> CHOICE{Escolher Ação}
    
    CHOICE -->|Listar| LIST[Listar Vale Transporte]
    CHOICE -->|Novo| NEW[Nova Solicitação]
    CHOICE -->|Calcular| CALC[Calcular Mês]
    CHOICE -->|Exportar| EXPORT[Exportar Relatório]
    
    subgraph "Fluxo Listagem"
        LIST --> FILTERS[Aplicar Filtros]
        FILTERS --> QUERY[Consultar Banco]
        QUERY --> STATS[Calcular Estatísticas]
        STATS --> DISPLAY[Exibir Resultados]
    end
    
    subgraph "Fluxo Nova Solicitação"
        NEW --> FORM[Formulário Cadastro]
        FORM --> VALIDATE{Validar Dados}
        VALIDATE -->|Erro| FORM
        VALIDATE -->|OK| SAVE[Salvar no Banco]
        SAVE --> NOTIFY[Notificar Sucesso]
    end
    
    subgraph "Fluxo Cálculo Mensal"
        CALC --> CONFIRM{Confirmar Ação}
        CONFIRM -->|Não| MENU
        CONFIRM -->|Sim| PROCESS[Processar Todos]
        PROCESS --> UPDATE[Atualizar Valores]
        UPDATE --> RESULT[Exibir Resultado]
    end
    
    DISPLAY --> END([Fim])
    NOTIFY --> END
    RESULT --> END
    EXPORT --> END
```

## Fluxo Detalhado - Nova Solicitação Vale Transporte

```mermaid
sequenceDiagram
    participant U as Usuário
    participant C as Controller
    participant S as Service
    participant R as Repository
    participant DB as Database
    
    U->>C: Acessar /vale-transporte/novo
    C->>S: Buscar colaboradores ativos
    S->>R: findByAtivoTrue()
    R->>DB: SELECT * FROM colaboradores WHERE ativo=true
    DB-->>R: Lista colaboradores
    R-->>S: Lista colaboradores
    S-->>C: Lista colaboradores
    C-->>U: Exibir formulário
    
    U->>C: Submeter formulário
    C->>C: Validar dados
    
    alt Dados válidos
        C->>S: Salvar vale transporte
        S->>S: Calcular valores automaticamente
        S->>R: save(valeTransporte)
        R->>DB: INSERT vale_transporte
        DB-->>R: ID gerado
        R-->>S: Vale salvo
        S-->>C: Sucesso
        C-->>U: Redirect + mensagem sucesso
    else Dados inválidos
        C-->>U: Exibir erros de validação
    end
```

## Fluxo de Cálculo Automático de Valores

```mermaid
flowchart TD
    TRIGGER[Trigger: Salvar/Atualizar] --> CHECK{Dados Completos?}
    CHECK -->|Não| SKIP[Pular Cálculo]
    CHECK -->|Sim| CALC_TOTAL[Calcular Valor Total Mês]
    
    CALC_TOTAL --> FORMULA1[Total = Dias Úteis × Viagens/Dia × Valor Passagem]
    FORMULA1 --> GET_SALARY[Obter Salário Colaborador]
    GET_SALARY --> CALC_DISCOUNT[Calcular Desconto]
    
    CALC_DISCOUNT --> FORMULA2[Desconto = min(Total, Salário × 6%)]
    FORMULA2 --> CALC_SUBSIDY[Calcular Subsídio Empresa]
    CALC_SUBSIDY --> FORMULA3[Subsídio = Total - Desconto]
    
    FORMULA3 --> SAVE_VALUES[Salvar Valores Calculados]
    SAVE_VALUES --> END_CALC[Fim Cálculo]
    
    SKIP --> END_CALC
```

## Estrutura de Dados - Vale Transporte

```mermaid
erDiagram
    ValeTransporte {
        Long id PK
        Integer mesReferencia
        Integer anoReferencia
        Integer diasUteis
        Integer viagensDia
        BigDecimal valorPassagem
        BigDecimal valorTotalMes
        BigDecimal percentualDesconto
        BigDecimal valorDesconto
        BigDecimal valorSubsidioEmpresa
        String linhaOnibus
        String enderecoOrigem
        String enderecoDestino
        StatusValeTransporte status
        LocalDate dataAdesao
        LocalDate dataCancelamento
        String observacoes
        LocalDateTime dataCriacao
    }
    
    Colaborador {
        Long id PK
        String nome
        String cpf
        String email
        BigDecimal salario
        Boolean ativo
    }
    
    Departamento {
        Long id PK
        String nome
    }
    
    ValeTransporte ||--|| Colaborador : belongs_to
    Colaborador ||--|| Departamento : belongs_to
```

## Endpoints da API

```mermaid
graph LR
    subgraph "Endpoints Vale Transporte"
        GET1[GET /listar] --> LIST_ALL[Listar todos]
        GET2[GET /novo] --> NEW_FORM[Formulário novo]
        POST1[POST /salvar] --> SAVE[Salvar vale]
        GET3[GET /editar/{id}] --> EDIT_FORM[Formulário edição]
        POST2[POST /atualizar/{id}] --> UPDATE[Atualizar vale]
        GET4[GET /deletar/{id}] --> DELETE[Deletar vale]
        
        GET5[GET /api/estatisticas] --> STATS[Estatísticas]
        POST3[POST /api/calcular-mes] --> CALC_MONTH[Calcular mês]
        GET6[GET /api/relatorio] --> REPORT[Gerar relatório]
    end
```

## Estados do Vale Transporte

```mermaid
stateDiagram-v2
    [*] --> ATIVO : Criar nova solicitação
    ATIVO --> SUSPENSO : Suspender temporariamente
    SUSPENSO --> ATIVO : Reativar
    ATIVO --> CANCELADO : Cancelar definitivamente
    SUSPENSO --> CANCELADO : Cancelar definitivamente
    CANCELADO --> [*] : Remover registro
    
    note right of ATIVO
        Vale ativo
        Cálculos incluídos
        Descontos aplicados
    end note
    
    note right of SUSPENSO
        Vale suspenso
        Cálculos pausados
        Descontos pausados
    end note
    
    note right of CANCELADO
        Vale cancelado
        Sem cálculos
        Sem descontos
    end note
```

## Próximos Passos da Implementação

1. **Expandir ValeTransporteService** - Adicionar métodos para estatísticas
2. **Melhorar ValeTransporteController** - Endpoints para dados reais
3. **Criar DTOs** - Para transferência de dados de estatísticas
4. **Integrar Templates** - Conectar com dados reais do banco
5. **Implementar Cálculo em Lote** - Para processamento mensal
6. **Adicionar Exportação** - Relatórios em PDF/Excel
7. **Testes** - Validar todas as funcionalidades

## Benefícios da Implementação

- ✅ **Automatização** - Cálculos automáticos de valores
- ✅ **Compliance** - Respeita limite legal de 6% do salário
- ✅ **Auditoria** - Histórico completo de alterações
- ✅ **Flexibilidade** - Diferentes tipos de transporte e trajetos
- ✅ **Relatórios** - Estatísticas em tempo real
- ✅ **UX** - Interface intuitiva e responsiva