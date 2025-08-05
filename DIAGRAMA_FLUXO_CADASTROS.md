# 🔄 Diagrama de Fluxo: Cadastro de Usuários e Colaboradores

## 📊 Fluxo Visual Completo

```mermaid
flowchart TD
    A[👤 Nova Pessoa] --> B{É Funcionário da Empresa?}
    
    B -->|Sim| C[📋 FLUXO COLABORADOR]
    B -->|Não| D[🔐 FLUXO USUÁRIO EXTERNO]
    
    %% FLUXO COLABORADOR
    C --> E[👥 RH: Processo de Admissão]
    E --> F[📝 Cadastro no Sistema RH]
    F --> G[🏢 Definir Cargo e Departamento]
    G --> H[📊 Estabelecer Hierarquia]
    H --> I{Precisa de Acesso ao Sistema?}
    
    I -->|Sim| J[📋 Solicitação de Acesso]
    I -->|Não| K[✅ Colaborador Cadastrado]
    
    J --> L[👨‍💼 Aprovação do Gestor]
    L --> M[💻 TI: Criação de Usuário]
    M --> N[🔗 Vinculação Colaborador-Usuário]
    N --> O[🔐 Configuração de Permissões]
    O --> P[📧 Envio de Credenciais]
    P --> Q[✅ Usuário Ativo]
    
    %% FLUXO USUÁRIO EXTERNO
    D --> R[📋 Solicitação Direta]
    R --> S[👨‍💼 Aprovação Diretoria]
    S --> T[💻 TI: Criação de Usuário]
    T --> U[🔐 Nível Restrito]
    U --> V[📧 Envio de Credenciais]
    V --> W[✅ Usuário Externo Ativo]
    
    %% ESTILOS
    classDef rhProcess fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef tiProcess fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef decision fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef success fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    
    class E,F,G,H rhProcess
    class M,N,O,P,T,U,V tiProcess
    class B,I,L,S decision
    class K,Q,W success
```

---

## 🎯 Matriz de Responsabilidades

```mermaid
flowchart LR
    subgraph "👥 DEPARTAMENTO RH"
        A1[Cadastro de Colaboradores]
        A2[Processo de Admissão]
        A3[Definição de Cargos]
        A4[Hierarquia Organizacional]
    end
    
    subgraph "💻 DEPARTAMENTO TI"
        B1[Cadastro de Usuários]
        B2[Configuração de Permissões]
        B3[Gestão de Acessos]
        B4[Suporte Técnico]
    end
    
    subgraph "👨‍💼 GESTORES"
        C1[Aprovação de Acessos]
        C2[Definição de Necessidades]
        C3[Supervisão de Equipe]
    end
    
    A1 -.-> B1
    A3 -.-> B2
    C1 --> B1
    C2 --> A1
```

---

## 🔐 Níveis de Acesso por Área

```mermaid
flowchart TD
    subgraph "🏢 ESTRUTURA ORGANIZACIONAL"
        MASTER[🔴 MASTER<br/>Acesso Total]
        ADMIN[🟠 ADMIN<br/>Administrativo]
        GERENTE[🟡 GERENTE<br/>Gerencial]
        COORD[🟢 COORDENADOR<br/>Coordenação]
        SUPER[🔵 SUPERVISOR<br/>Supervisão]
        ANALISTA[🟣 ANALISTA<br/>Analítico]
        OPER[⚫ OPERACIONAL<br/>Operacional]
    end
    
    subgraph "👥 ÁREA RH"
        RH1[Gerente RH - GERENTE]
        RH2[Analista RH - ANALISTA]
        RH3[Assistente RH - OPERACIONAL]
    end
    
    subgraph "💻 ÁREA TI"
        TI1[Gerente TI - GERENTE]
        TI2[Analista Sistemas - ANALISTA]
        TI3[Suporte - OPERACIONAL]
    end
    
    subgraph "💰 ÁREA FINANCEIRO"
        FIN1[Controller - GERENTE]
        FIN2[Analista Financeiro - ANALISTA]
        FIN3[Assistente - OPERACIONAL]
    end
    
    subgraph "🛒 ÁREA COMERCIAL"
        COM1[Gerente Comercial - GERENTE]
        COM2[Vendedor - OPERACIONAL]
        COM3[Representante - OPERACIONAL]
    end
    
    GERENTE --> RH1
    GERENTE --> TI1
    GERENTE --> FIN1
    GERENTE --> COM1
    
    ANALISTA --> RH2
    ANALISTA --> TI2
    ANALISTA --> FIN2
    
    OPER --> RH3
    OPER --> TI3
    OPER --> FIN3
    OPER --> COM2
    OPER --> COM3
```

---

## 📋 Processo de Aprovação

```mermaid
sequenceDiagram
    participant G as 👨‍💼 Gestor
    participant RH as 👥 RH
    participant TI as 💻 TI
    participant U as 👤 Usuário
    
    Note over G,U: NOVO FUNCIONÁRIO
    
    G->>RH: 1. Solicitação de Contratação
    RH->>RH: 2. Processo de Admissão
    RH->>RH: 3. Cadastro de Colaborador
    
    Note over G,U: ACESSO AO SISTEMA
    
    G->>TI: 4. Solicitação de Acesso
    TI->>G: 5. Formulário de Justificativa
    G->>TI: 6. Aprovação + Justificativa
    TI->>TI: 7. Criação de Usuário
    TI->>TI: 8. Configuração de Permissões
    TI->>U: 9. Envio de Credenciais
    U->>TI: 10. Primeiro Login (Troca Senha)
    
    Note over G,U: USUÁRIO ATIVO
```

---

## 🔄 Estados do Usuário/Colaborador

```mermaid
stateDiagram-v2
    [*] --> Solicitado
    
    state "👤 COLABORADOR" as Colaborador {
        Solicitado --> EmAdmissao : RH inicia processo
        EmAdmissao --> Ativo : Contrato assinado
        Ativo --> Inativo : Desligamento
        Ativo --> Afastado : Licença/Férias
        Afastado --> Ativo : Retorno
        Inativo --> [*]
    }
    
    state "🔐 USUÁRIO" as Usuario {
        [*] --> Pendente : Solicitação
        Pendente --> Aprovado : Gestor aprova
        Aprovado --> Criado : TI cria usuário
        Criado --> PrimeiroLogin : Credenciais enviadas
        PrimeiroLogin --> Ativo : Senha alterada
        Ativo --> Bloqueado : Tentativas inválidas
        Ativo --> Suspenso : Violação política
        Ativo --> Inativo : Desligamento
        Bloqueado --> Ativo : Desbloqueio
        Suspenso --> Ativo : Reativação
        Inativo --> [*]
    }
```

---

## 📊 Dashboard de Controle

```mermaid
flowchart LR
    subgraph "📈 MÉTRICAS RH"
        M1[👥 Total Colaboradores]
        M2[📈 Admissões/Mês]
        M3[📉 Desligamentos/Mês]
        M4[🏢 Por Departamento]
    end
    
    subgraph "🔐 MÉTRICAS TI"
        M5[👤 Total Usuários]
        M6[✅ Usuários Ativos]
        M7[❌ Usuários Inativos]
        M8[🔒 Bloqueados]
    end
    
    subgraph "⚠️ ALERTAS"
        A1[🚨 Usuários sem Colaborador]
        A2[🚨 Colaboradores sem Usuário]
        A3[🚨 Acessos Suspeitos]
        A4[🚨 Senhas Expiradas]
    end
    
    M1 --> A2
    M5 --> A1
    M6 --> A3
    M6 --> A4
```

---

## 🎯 Checklist de Implementação

### ✅ Fase 1: Preparação
- [ ] Definir políticas de acesso
- [ ] Treinar equipe RH
- [ ] Treinar equipe TI
- [ ] Criar templates de documentos

### ✅ Fase 2: Implementação
- [ ] Configurar sistema
- [ ] Migrar dados existentes
- [ ] Testar fluxos
- [ ] Validar permissões

### ✅ Fase 3: Operação
- [ ] Monitorar métricas
- [ ] Ajustar processos
- [ ] Treinar novos usuários
- [ ] Manter documentação atualizada

---

*Este diagrama complementa o documento principal de fluxo de cadastros*