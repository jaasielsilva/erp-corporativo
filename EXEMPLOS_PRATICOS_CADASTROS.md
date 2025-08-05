# 🎯 Exemplos Práticos: Cadastro de Usuários e Colaboradores

## 📋 Cenários Reais de Implementação

---

## 👤 **EXEMPLO 1: NOVO FUNCIONÁRIO - ANALISTA DE RH**

### 📝 **Etapa 1: Cadastro como Colaborador (RH)**
```
👥 RESPONSÁVEL: Maria Silva (Gerente de RH)
📅 DATA: 15/01/2024
🏢 DEPARTAMENTO: Recursos Humanos
```

**Dados do Colaborador:**
- **Nome:** João Santos Silva
- **CPF:** 123.456.789-00
- **Email Pessoal:** joao.santos@email.com
- **Cargo:** Analista de RH
- **Departamento:** Recursos Humanos
- **Data Admissão:** 15/01/2024
- **Salário:** R$ 4.500,00
- **Superior Imediato:** Maria Silva (Gerente de RH)
- **Status:** Ativo

### 🔐 **Etapa 2: Solicitação de Acesso ao Sistema**
```
📋 SOLICITANTE: Maria Silva (Gerente de RH)
📅 DATA SOLICITAÇÃO: 15/01/2024
💻 RESPONSÁVEL TI: Carlos Oliveira (Analista de Sistemas)
```

**Justificativa:**
> "Novo analista de RH precisa de acesso ao sistema para:
> - Cadastrar novos colaboradores
> - Consultar dados de funcionários
> - Gerar relatórios de RH
> - Acessar módulo de benefícios"

### ⚙️ **Etapa 3: Criação do Usuário (TI)**
```
💻 CRIADO POR: Carlos Oliveira
📅 DATA CRIAÇÃO: 15/01/2024
🔗 VINCULAÇÃO: João Santos Silva (Colaborador ID: 1001)
```

**Configurações do Usuário:**
- **Email (Login):** joao.santos@empresa.com.br
- **Senha Temporária:** TempRH2024!
- **Nível de Acesso:** ANALISTA
- **Perfis:** USER, RH
- **Departamento:** Recursos Humanos

### 📊 **Resultado: Acesso ao Sistema**
**Módulos Visíveis para João:**
- ✅ Dashboard
- ✅ Meu Perfil
- ✅ Recursos Humanos (completo)
  - Colaboradores (listar, cadastrar, editar)
  - Benefícios (consultar, gerenciar)
  - Ponto e Escalas
  - Férias
  - Treinamentos
- ✅ Serviços
- ✅ Agenda
- ❌ Vendas (não tem acesso)
- ❌ Financeiro (não tem acesso)
- ❌ Estoque (não tem acesso)

---

## 💰 **EXEMPLO 2: FUNCIONÁRIO EXISTENTE - ACESSO FINANCEIRO**

### 📝 **Situação Inicial**
```
👤 COLABORADOR EXISTENTE: Ana Costa
🏢 CARGO: Assistente Administrativo
📅 ADMISSÃO: 01/06/2023
🔐 ACESSO ATUAL: Não possui usuário no sistema
```

### 🔄 **Mudança de Função**
```
📅 DATA: 20/01/2024
🔄 NOVA FUNÇÃO: Assistente Financeiro
🏢 NOVO DEPARTAMENTO: Financeiro
👨‍💼 NOVO SUPERIOR: Roberto Lima (Controller)
```

### 📋 **Solicitação de Acesso**
```
📋 SOLICITANTE: Roberto Lima (Controller)
💻 APROVADOR TI: Carlos Oliveira
📅 DATA: 20/01/2024
```

**Justificativa:**
> "Ana foi promovida para Assistente Financeiro e precisa de acesso para:
> - Lançar contas a pagar e receber
> - Consultar relatórios financeiros
> - Acompanhar fluxo de caixa
> - Processar conciliações bancárias"

### ⚙️ **Configuração do Usuário**
```
💻 CRIADO POR: Carlos Oliveira
📅 DATA: 20/01/2024
🔗 VINCULAÇÃO: Ana Costa (Colaborador ID: 0856)
```

**Dados do Usuário:**
- **Email:** ana.costa@empresa.com.br
- **Nível de Acesso:** OPERACIONAL
- **Perfis:** USER, FINANCEIRO
- **Departamento:** Financeiro

### 📊 **Resultado: Módulos Visíveis**
- ✅ Dashboard
- ✅ Meu Perfil
- ✅ Financeiro
  - Contas a Pagar
  - Contas a Receber
  - Fluxo de Caixa
  - Transferências
- ✅ Serviços
- ✅ Agenda
- ❌ RH (não tem acesso)
- ❌ Vendas (não tem acesso)

---

## 🏢 **EXEMPLO 3: CONSULTOR EXTERNO - ACESSO TEMPORÁRIO**

### 📝 **Situação**
```
👤 PESSOA: Dr. Pedro Consultoria Ltda
🏢 TIPO: Consultor Externo
📅 PERÍODO: 01/02/2024 a 31/05/2024
🎯 PROJETO: Implementação ISO 9001
```

### 📋 **Solicitação Especial**
```
📋 SOLICITANTE: Diretor Geral
💻 APROVADOR: Diretor de TI
📅 DATA: 25/01/2024
⏰ PRAZO: 4 meses
```

**Justificativa:**
> "Consultor precisa de acesso limitado para:
> - Consultar processos da empresa
> - Acessar documentos de qualidade
> - Gerar relatórios de auditoria
> - Não deve ter acesso a dados financeiros ou pessoais"

### ⚙️ **Configuração Restrita**
```
💻 CRIADO POR: Carlos Oliveira
📅 DATA: 25/01/2024
🔗 VINCULAÇÃO: Nenhuma (usuário externo)
⏰ EXPIRAÇÃO: 31/05/2024
```

**Dados do Usuário:**
- **Email:** pedro.consultor@empresa.com.br
- **Nível de Acesso:** CONSULTOR
- **Perfis:** USER (limitado)
- **Restrições:** Sem acesso a dados sensíveis

### 📊 **Resultado: Acesso Limitado**
- ✅ Dashboard (limitado)
- ✅ Meu Perfil
- ✅ Documentos (apenas consulta)
- ✅ Processos (apenas consulta)
- ✅ Suporte
- ❌ RH (bloqueado)
- ❌ Financeiro (bloqueado)
- ❌ Vendas (bloqueado)
- ❌ Configurações (bloqueado)

---

## 🛒 **EXEMPLO 4: VENDEDOR EXTERNO - ACESSO COMERCIAL**

### 📝 **Situação**
```
👤 PESSOA: Carlos Vendas
🏢 TIPO: Representante Comercial
📅 CONTRATO: Prestação de Serviços
🎯 ÁREA: Região Sul
```

### 📋 **Necessidade de Acesso**
```
📋 SOLICITANTE: Gerente Comercial
📅 DATA: 10/02/2024
🎯 OBJETIVO: Gestão de vendas na região
```

**Justificativa:**
> "Representante precisa acessar:
> - Cadastro de clientes da região Sul
> - Registro de vendas e propostas
> - Consulta de produtos e preços
> - Agenda de visitas"

### ⚙️ **Configuração Comercial**
```
💻 CRIADO POR: Carlos Oliveira
📅 DATA: 10/02/2024
🔗 VINCULAÇÃO: Nenhuma (representante)
🌍 RESTRIÇÃO: Apenas região Sul
```

**Dados do Usuário:**
- **Email:** carlos.vendas@empresa.com.br
- **Nível de Acesso:** OPERACIONAL
- **Perfis:** USER, VENDAS (limitado)
- **Filtros:** Região = Sul

### 📊 **Resultado: Acesso Comercial**
- ✅ Dashboard (vendas)
- ✅ Meu Perfil
- ✅ Clientes (apenas região Sul)
- ✅ Vendas (apenas suas vendas)
- ✅ Produtos (consulta)
- ✅ Agenda
- ❌ Todos os outros módulos

---

## 🎓 **EXEMPLO 5: ESTAGIÁRIO - ACESSO SUPERVISIONADO**

### 📝 **Situação**
```
👤 PESSOA: Julia Estudante
🏢 TIPO: Estagiária
📅 PERÍODO: 6 meses
🎯 ÁREA: Marketing
👨‍🏫 SUPERVISOR: Gerente de Marketing
```

### 📋 **Processo de Cadastro**

#### **Etapa 1: Cadastro como Colaborador (RH)**
- **Nome:** Julia Estudante
- **CPF:** 987.654.321-00
- **Cargo:** Estagiária de Marketing
- **Departamento:** Marketing
- **Supervisor:** Gerente de Marketing
- **Período:** 6 meses
- **Bolsa:** R$ 800,00

#### **Etapa 2: Criação de Usuário (TI)**
- **Email:** julia.estagiaria@empresa.com.br
- **Nível de Acesso:** ESTAGIARIO
- **Perfis:** USER (básico)
- **Supervisão:** Todas as ações são logadas

### 📊 **Resultado: Acesso Supervisionado**
- ✅ Dashboard (básico)
- ✅ Meu Perfil
- ✅ Marketing (apenas consulta)
- ✅ Materiais de Marketing
- ✅ Agenda
- ✅ Treinamentos
- ❌ Módulos sensíveis (RH, Financeiro)
- 📝 **Todas as ações são registradas para supervisão**

---

## 📊 **RESUMO COMPARATIVO**

| Usuário | Tipo | Nível | Módulos Principais | Restrições |
|---------|------|-------|-------------------|------------|
| João (Analista RH) | Funcionário | ANALISTA | RH Completo | Sem acesso comercial/financeiro |
| Ana (Assist. Financeiro) | Funcionário | OPERACIONAL | Financeiro | Sem acesso RH/vendas |
| Pedro (Consultor) | Externo | CONSULTOR | Documentos | Acesso temporário, sem dados sensíveis |
| Carlos (Vendedor) | Representante | OPERACIONAL | Vendas | Apenas região Sul |
| Julia (Estagiária) | Estagiária | ESTAGIARIO | Marketing | Acesso supervisionado |

---

## 🔄 **Fluxo de Aprovação Resumido**

### ✅ **Funcionários Internos**
1. RH cadastra colaborador
2. Gestor solicita acesso ao sistema
3. TI cria usuário e vincula ao colaborador
4. Configuração automática baseada no cargo

### ✅ **Usuários Externos**
1. Solicitação direta ao TI
2. Aprovação da diretoria
3. Criação com restrições específicas
4. Prazo de validade definido

### ✅ **Casos Especiais**
1. Estagiários: Acesso supervisionado
2. Terceirizados: Acesso muito limitado
3. Consultores: Acesso temporário
4. Representantes: Acesso regional

---

## 🎯 **Próximos Passos para Implementação**

1. **Validar Exemplos:** Revisar cenários com stakeholders
2. **Criar Templates:** Formulários padronizados para cada tipo
3. **Treinar Equipes:** Capacitar RH e TI nos novos processos
4. **Implementar Gradualmente:** Começar com um tipo de usuário
5. **Monitorar e Ajustar:** Acompanhar métricas e melhorar processos

---

*Estes exemplos servem como guia para implementação prática do sistema de cadastros*