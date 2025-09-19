# Fluxograma Completo - Módulo de Suporte

## Fluxo Principal do Sistema de Suporte

```mermaid
graph TD
    A[Solicitante cria chamado] --> B[Sistema gera número único]
    B --> C[Define SLA baseado na prioridade]
    C --> D[Chamado salvo com status ABERTO]
    D --> E{Atribuição automática ativada?}
    
    E -->|Sim| F[Buscar colaboradores disponíveis]
    E -->|Não| G[Chamado fica no pool de atribuição]
    
    F --> H[Verificar carga de trabalho]
    H --> I[Selecionar colaborador com menor carga]
    I --> J[Atribuir colaborador]
    
    G --> K[Gestor atribui manualmente]
    K --> J
    
    J --> L[Enviar notificação por email]
    L --> M[Enviar notificação interna]
    M --> N[Status alterado para EM_ANDAMENTO]
    
    N --> O[Colaborador trabalha no chamado]
    O --> P{Chamado resolvido?}
    
    P -->|Não| Q[Colaborador adiciona comentários]
    Q --> O
    
    P -->|Sim| R[Status alterado para RESOLVIDO]
    R --> S[Registrar data de resolução]
    S --> T[Notificar solicitante por email]
    T --> U[Solicitar avaliação]
    
    U --> V{Solicitante avalia?}
    V -->|Sim| W[Registrar avaliação]
    V -->|Não| X[Aguardar 7 dias]
    
    W --> Y[Status FECHADO]
    X --> Z{7 dias passaram?}
    Z -->|Sim| Y
    Z -->|Não| X
    
    Y --> AA[Fim do processo]
    
    style A fill:#e1f5fe
    style J fill:#c8e6c9
    style R fill:#fff3e0
    style Y fill:#f3e5f5
```

## Fluxo de Atribuição de Colaboradores

```mermaid
graph TD
    A[Chamado criado] --> B{Tipo de atribuição}
    
    B -->|Manual| C[Gestor seleciona colaborador]
    B -->|Automática| D[Sistema busca colaboradores]
    
    C --> E[Verificar se colaborador existe]
    E -->|Não| F[Erro: Colaborador não encontrado]
    E -->|Sim| G[Verificar disponibilidade]
    
    D --> H[Filtrar por cargo/nível]
    H --> I[Verificar disponibilidade de cada um]
    I --> J[Calcular carga de trabalho atual]
    J --> K[Selecionar com menor carga]
    K --> G
    
    G -->|Indisponível| L[Buscar próximo colaborador]
    G -->|Disponível| M[Atribuir chamado]
    
    L --> N{Há outros colaboradores?}
    N -->|Sim| G
    N -->|Não| O[Adicionar ao backlog]
    
    M --> P[Registrar atribuição no banco]
    P --> Q[Enviar email de notificação]
    Q --> R[Criar notificação interna]
    R --> S[Atualizar status do chamado]
    
    O --> T[Notificar gestores sobre backlog]
    
    style M fill:#c8e6c9
    style F fill:#ffcdd2
    style O fill:#fff3e0
```

## Fluxo de Notificações

```mermaid
graph TD
    A[Evento do sistema] --> B{Tipo de evento}
    
    B -->|Novo chamado atribuído| C[Notificar colaborador]
    B -->|Chamado resolvido| D[Notificar solicitante]
    B -->|SLA próximo do vencimento| E[Notificar colaborador e gestor]
    B -->|Chamado reaberto| F[Notificar colaborador]
    B -->|Avaliação recebida| G[Notificar gestor]
    
    C --> H[Preparar template de email]
    D --> I[Preparar template de resolução]
    E --> J[Preparar template de alerta SLA]
    F --> K[Preparar template de reabertura]
    G --> L[Preparar template de avaliação]
    
    H --> M[Enviar email corporativo]
    I --> M
    J --> M
    K --> M
    L --> M
    
    M --> N{Email enviado com sucesso?}
    N -->|Sim| O[Criar notificação interna]
    N -->|Não| P[Registrar erro no log]
    
    O --> Q[Salvar notificação no banco]
    Q --> R[Atualizar contador de notificações]
    R --> S[Exibir badge no dashboard]
    
    P --> T[Tentar reenvio em 5 minutos]
    T --> U{Tentativas < 3?}
    U -->|Sim| M
    U -->|Não| V[Marcar como falha definitiva]
    
    style M fill:#e3f2fd
    style P fill:#ffcdd2
    style V fill:#ffcdd2
```

## Fluxo de Monitoramento de SLA

```mermaid
graph TD
    A[Scheduler executa a cada 30 min] --> B[Buscar chamados ativos]
    B --> C[Para cada chamado]
    
    C --> D[Calcular tempo restante para SLA]
    D --> E{Tempo restante}
    
    E -->|< 1 hora| F[Alerta CRÍTICO]
    E -->|< 4 horas| G[Alerta de AVISO]
    E -->|SLA vencido| H[Alerta de VIOLAÇÃO]
    E -->|> 4 horas| I[Sem alerta necessário]
    
    F --> J[Notificar colaborador e gestor]
    G --> K[Notificar colaborador]
    H --> L[Notificar gestor e diretor]
    
    J --> M[Registrar alerta no banco]
    K --> M
    L --> M
    
    M --> N[Atualizar métricas de SLA]
    N --> O[Próximo chamado]
    
    I --> O
    O --> P{Há mais chamados?}
    P -->|Sim| C
    P -->|Não| Q[Gerar relatório de SLA]
    
    Q --> R[Atualizar dashboard]
    R --> S[Fim da execução]
    
    style F fill:#ffcdd2
    style G fill:#fff3e0
    style H fill:#f44336,color:#fff
```

## Fluxo de Cargos e Níveis de Suporte

```mermaid
graph TD
    A[Colaborador cadastrado] --> B[Definir cargo/nível]
    
    B --> C{Tipo de cargo}
    C -->|Suporte Nível 1| D[Chamados básicos]
    C -->|Suporte Nível 2| E[Chamados intermediários]
    C -->|Técnico Especialista| F[Chamados complexos]
    C -->|Coordenador| G[Gestão e escalação]
    
    D --> H[Prioridade: BAIXA, MÉDIA]
    E --> I[Prioridade: MÉDIA, ALTA]
    F --> J[Prioridade: ALTA, CRÍTICA]
    G --> K[Todos os tipos]
    
    H --> L[Limite: 10 chamados ativos]
    I --> M[Limite: 8 chamados ativos]
    J --> N[Limite: 5 chamados ativos]
    K --> O[Limite: 15 chamados ativos]
    
    L --> P[Atribuição automática]
    M --> P
    N --> P
    O --> P
    
    P --> Q[Sistema verifica disponibilidade]
    Q --> R{Dentro do limite?}
    
    R -->|Sim| S[Atribuir chamado]
    R -->|Não| T[Buscar próximo colaborador]
    
    S --> U[Notificar colaborador]
    T --> V{Há outros disponíveis?}
    V -->|Sim| Q
    V -->|Não| W[Adicionar ao backlog]
    
    style D fill:#e8f5e8
    style E fill:#fff3e0
    style F fill:#ffebee
    style G fill:#e3f2fd
```

## Estrutura de Dados do Colaborador

```mermaid
erDiagram
    COLABORADOR {
        Long id PK
        String nome
        String matricula UK
        String email
        String cargo
        String nivel
        Boolean ativo
        Integer limiteChamados
        LocalDateTime dataAdmissao
    }
    
    CHAMADO {
        Long id PK
        String numero UK
        String assunto
        String descricao
        StatusChamado status
        Prioridade prioridade
        Long colaboradorResponsavelId FK
        String solicitanteNome
        String solicitanteEmail
        String categoria
        LocalDateTime dataAbertura
        LocalDateTime dataResolucao
        LocalDateTime slaVencimento
        Integer avaliacao
        String comentarioAvaliacao
    }
    
    NOTIFICACAO {
        Long id PK
        Long colaboradorId FK
        Long chamadoId FK
        String tipo
        String titulo
        String mensagem
        Boolean lida
        LocalDateTime dataEnvio
    }
    
    COLABORADOR ||--o{ CHAMADO : "responsavel"
    COLABORADOR ||--o{ NOTIFICACAO : "recebe"
    CHAMADO ||--o{ NOTIFICACAO : "gera"
```

## Fluxo de Informações do Colaborador

```mermaid
graph TD
    A[Chamado atribuído ao colaborador] --> B[Sistema coleta informações]
    
    B --> C[Nome: João Silva]
    B --> D[Matrícula: 12345]
    B --> E[Cargo: Suporte Nível 2]
    B --> F[Email: joao.silva@empresa.com]
    
    C --> G[Personalizar notificação]
    D --> G
    E --> G
    F --> G
    
    G --> H[Template de email]
    H --> I["Olá João Silva (Mat: 12345)"]
    I --> J["Cargo: Suporte Nível 2"]
    J --> K["Novo chamado atribuído"]
    
    K --> L[Enviar para: joao.silva@empresa.com]
    L --> M[Notificação interna no sistema]
    
    M --> N[Dashboard personalizado]
    N --> O["Bem-vindo, João Silva"]
    O --> P["Seus chamados (Nível 2)"]
    
    style G fill:#e3f2fd
    style L fill:#c8e6c9
    style N fill:#fff3e0
```

## Processo de Escalação

```mermaid
graph TD
    A[Chamado com SLA próximo] --> B{Nível do colaborador atual}
    
    B -->|Nível 1| C[Escalar para Nível 2]
    B -->|Nível 2| D[Escalar para Técnico]
    B -->|Técnico| E[Escalar para Coordenador]
    B -->|Coordenador| F[Escalar para Gerência]
    
    C --> G[Buscar colaborador Nível 2 disponível]
    D --> H[Buscar Técnico disponível]
    E --> I[Buscar Coordenador disponível]
    F --> J[Notificar Gerência]
    
    G --> K{Encontrou colaborador?}
    H --> K
    I --> K
    
    K -->|Sim| L[Reatribuir chamado]
    K -->|Não| M[Manter com colaborador atual]
    
    L --> N[Notificar novo colaborador]
    L --> O[Notificar colaborador anterior]
    
    M --> P[Aumentar prioridade]
    P --> Q[Notificar gestores]
    
    J --> R[Decisão gerencial]
    
    style L fill:#c8e6c9
    style M fill:#fff3e0
    style P fill:#ffcdd2
```

## Dashboard de Métricas

```mermaid
graph TD
    A[Dashboard Principal] --> B[Métricas por Colaborador]
    A --> C[Métricas de SLA]
    A --> D[Métricas Gerais]
    
    B --> E[João Silva - Nível 2]
    E --> F[8 chamados ativos]
    E --> G[Tempo médio: 4h]
    E --> H[Avaliação: 4.5/5]
    
    B --> I[Maria Santos - Técnica]
    I --> J[3 chamados ativos]
    I --> K[Tempo médio: 2h]
    I --> L[Avaliação: 4.8/5]
    
    C --> M[SLA em dia: 85%]
    C --> N[Próximos a vencer: 5]
    C --> O[Vencidos hoje: 2]
    
    D --> P[Total de chamados: 150]
    D --> Q[Resolvidos hoje: 25]
    D --> R[Satisfação geral: 4.3/5]
    
    style E fill:#e8f5e8
    style I fill:#e8f5e8
    style M fill:#c8e6c9
    style O fill:#ffcdd2
```

---

## Legenda dos Fluxogramas

### Cores e Significados
- 🟢 **Verde**: Processos bem-sucedidos
- 🟡 **Amarelo**: Processos de atenção/aviso
- 🔴 **Vermelho**: Processos de erro/crítico
- 🔵 **Azul**: Processos informativos
- ⚪ **Branco**: Processos neutros

### Símbolos
- **Retângulo**: Processo/Ação
- **Losango**: Decisão/Condição
- **Círculo**: Início/Fim
- **Retângulo arredondado**: Dados/Informação

### Tipos de Notificação
1. **Email Corporativo**: Notificação formal via email
2. **Notificação Interna**: Alert no sistema/dashboard
3. **SMS** (futuro): Mensagem de texto para urgências
4. **WhatsApp** (futuro): Mensagem via WhatsApp Business

---

*Fluxograma atualizado em: Janeiro 2025*
*Versão: 1.0*