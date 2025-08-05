# 📋 Fluxo Empresarial: Cadastro de Usuários vs Colaboradores

## 🎯 Visão Geral

Este documento define o fluxo empresarial para cadastro de **Usuários** (acesso ao sistema) e **Colaboradores** (funcionários da empresa), estabelecendo as diferenças, responsabilidades e níveis de acesso.

---

## 👥 **USUÁRIOS vs COLABORADORES**

### 🔐 **USUÁRIOS** (Acesso ao Sistema)
- **Definição:** Pessoas que possuem login e senha para acessar o sistema ERP
- **Finalidade:** Acesso às funcionalidades do sistema conforme seu nível de permissão
- **Cadastro:** Realizado pelo departamento de TI ou Administradores
- **Vinculação:** Pode ou não estar vinculado a um colaborador

### 👔 **COLABORADORES** (Funcionários da Empresa)
- **Definição:** Funcionários formalmente contratados pela empresa
- **Finalidade:** Registro para RH, folha de pagamento, benefícios, etc.
- **Cadastro:** Realizado pelo departamento de RH
- **Vinculação:** Pode ou não ter acesso ao sistema (usuário)

---

## 🔄 **FLUXOS DE CADASTRO**

### 📝 **1. FLUXO DE CADASTRO DE COLABORADOR**

#### **Etapa 1: Admissão (RH)**
- ✅ **Responsável:** Departamento de RH
- ✅ **Quando:** Processo de admissão do funcionário
- ✅ **Dados Coletados:**
  - Informações pessoais (nome, CPF, RG, endereço)
  - Dados contratuais (cargo, departamento, salário)
  - Documentos (foto, documentos pessoais)
  - Informações bancárias
  - Dependentes e beneficiários

#### **Etapa 2: Registro no Sistema**
- ✅ **Acesso:** Apenas usuários com `podeAcessarRH = true`
- ✅ **Tela:** `/rh/colaboradores/novo`
- ✅ **Campos Obrigatórios:**
  - Nome completo
  - CPF (único)
  - Cargo
  - Departamento
  - Data de admissão
  - Status (Ativo/Inativo)

#### **Etapa 3: Definição de Hierarquia**
- ✅ **Superior Imediato:** Definido conforme organograma
- ✅ **Nível Hierárquico:** Baseado no cargo
- ✅ **Aprovadores:** Para férias, horas extras, etc.

---

### 🔐 **2. FLUXO DE CADASTRO DE USUÁRIO**

#### **Etapa 1: Solicitação de Acesso**
- ✅ **Solicitante:** Gestor direto ou RH
- ✅ **Justificativa:** Necessidade de acesso ao sistema
- ✅ **Aprovação:** Gerente da área + TI

#### **Etapa 2: Criação do Usuário (TI/Admin)**
- ✅ **Responsável:** Administradores ou TI
- ✅ **Acesso:** Apenas usuários com `podeGerenciarUsuarios = true`
- ✅ **Tela:** `/usuarios/novo`
- ✅ **Dados Necessários:**
  - Email (login único)
  - Senha temporária
  - Nível de acesso
  - Perfis de permissão
  - Vinculação com colaborador (opcional)

#### **Etapa 3: Configuração de Permissões**
- ✅ **Nível de Acesso:** Conforme função no organograma
- ✅ **Perfis:** Baseado na área de atuação
- ✅ **Departamento:** Define quais módulos pode acessar

---

## 🏢 **MATRIZ DE ACESSO POR ÁREA**

### 👥 **RECURSOS HUMANOS**
| Cargo | Nível Acesso | Pode Cadastrar Colaborador | Pode Cadastrar Usuário | Módulos Visíveis |
|-------|--------------|----------------------------|------------------------|------------------|
| Gerente de RH | GERENTE | ✅ Sim | ❌ Não | RH Completo, Relatórios |
| Analista de RH | ANALISTA | ✅ Sim | ❌ Não | RH Operacional |
| Assistente de RH | OPERACIONAL | ⚠️ Limitado | ❌ Não | RH Básico |

### 💻 **TECNOLOGIA DA INFORMAÇÃO**
| Cargo | Nível Acesso | Pode Cadastrar Colaborador | Pode Cadastrar Usuário | Módulos Visíveis |
|-------|--------------|----------------------------|------------------------|------------------|
| Gerente de TI | GERENTE | ❌ Não | ✅ Sim | TI, Usuários, Config |
| Analista de Sistemas | ANALISTA | ❌ Não | ✅ Sim | TI, Usuários |
| Suporte Técnico | OPERACIONAL | ❌ Não | ❌ Não | TI Básico |

### 💰 **FINANCEIRO**
| Cargo | Nível Acesso | Pode Cadastrar Colaborador | Pode Cadastrar Usuário | Módulos Visíveis |
|-------|--------------|----------------------------|------------------------|------------------|
| Controller | GERENTE | ❌ Não | ❌ Não | Financeiro Completo |
| Analista Financeiro | ANALISTA | ❌ Não | ❌ Não | Financeiro Operacional |
| Assistente Financeiro | OPERACIONAL | ❌ Não | ❌ Não | Financeiro Básico |

### 🛒 **COMERCIAL/VENDAS**
| Cargo | Nível Acesso | Pode Cadastrar Colaborador | Pode Cadastrar Usuário | Módulos Visíveis |
|-------|--------------|----------------------------|------------------------|------------------|
| Gerente Comercial | GERENTE | ❌ Não | ❌ Não | Vendas, Clientes, Relatórios |
| Vendedor | OPERACIONAL | ❌ Não | ❌ Não | Vendas, Clientes |
| Representante | OPERACIONAL | ❌ Não | ❌ Não | Vendas Limitadas |

---

## 🔄 **CENÁRIOS PRÁTICOS**

### 📋 **Cenário 1: Novo Funcionário**
1. **RH cadastra colaborador** → Sistema gera ficha funcional
2. **Gestor solicita acesso ao sistema** → Aprovação necessária
3. **TI/Admin cria usuário** → Vincula ao colaborador existente
4. **Usuário recebe credenciais** → Primeiro login obriga troca de senha

### 📋 **Cenário 2: Funcionário Existente Precisa de Acesso**
1. **Colaborador já existe** → Cadastrado pelo RH
2. **Gestor solicita acesso** → Justificativa de necessidade
3. **TI cria usuário** → Vincula ao colaborador existente
4. **Configuração de permissões** → Baseada no cargo/departamento

### 📋 **Cenário 3: Usuário Externo (Consultor/Terceirizado)**
1. **Não é colaborador** → Não passa pelo RH
2. **Solicitação direta ao TI** → Aprovação da diretoria
3. **Criação de usuário** → Sem vinculação com colaborador
4. **Acesso limitado** → Nível CONSULTOR ou TERCEIRIZADO

### 📋 **Cenário 4: Desligamento**
1. **RH inativa colaborador** → Status = Inativo
2. **TI desativa usuário** → Acesso bloqueado imediatamente
3. **Backup de dados** → Antes da exclusão definitiva
4. **Transferência de responsabilidades** → Para novo responsável

---

## ⚙️ **CONFIGURAÇÕES TÉCNICAS**

### 🔐 **Níveis de Acesso Disponíveis**
```
MASTER (1)      → Acesso total (nunca pode ser editado)
ADMIN (2)       → Acesso administrativo completo
GERENTE (3)     → Acesso gerencial com supervisão
COORDENADOR (4) → Acesso de coordenação departamental
SUPERVISOR (5)  → Acesso de supervisão de equipe
ANALISTA (6)    → Acesso analítico e operacional
OPERACIONAL (7) → Acesso operacional básico
USER (8)        → Acesso básico do sistema
ESTAGIARIO (9)  → Acesso limitado para estagiários
TERCEIRIZADO (10) → Acesso restrito para terceirizados
CONSULTOR (11)  → Acesso específico para consultores
VISITANTE (12)  → Acesso muito limitado
```

### 🏷️ **Perfis de Permissão**
- **ADMIN:** Todas as permissões
- **USER:** Permissões básicas de usuário
- **RH:** Permissões específicas de recursos humanos
- **FINANCEIRO:** Permissões específicas financeiras
- **VENDAS:** Permissões específicas comerciais

---

## 📊 **RELATÓRIOS E CONTROLES**

### 📈 **Relatórios Disponíveis**
- **Usuários Ativos:** Lista de usuários com acesso ao sistema
- **Colaboradores sem Acesso:** Funcionários que não têm usuário
- **Usuários sem Colaborador:** Usuários externos (consultores, etc.)
- **Acessos por Departamento:** Distribuição de usuários por área
- **Níveis de Acesso:** Quantidade por nível hierárquico

### 🔍 **Auditoria**
- **Log de Criação:** Quem criou, quando e por quê
- **Histórico de Alterações:** Mudanças de permissões
- **Último Acesso:** Controle de usuários inativos
- **Tentativas de Login:** Segurança e monitoramento

---

## ✅ **CHECKLIST DE IMPLEMENTAÇÃO**

### 🎯 **Para RH**
- [ ] Definir processo de admissão
- [ ] Treinar equipe no cadastro de colaboradores
- [ ] Estabelecer fluxo de aprovação para acesso ao sistema
- [ ] Criar templates de documentos necessários

### 🎯 **Para TI**
- [ ] Definir política de senhas
- [ ] Estabelecer processo de criação de usuários
- [ ] Configurar backup automático
- [ ] Implementar logs de auditoria

### 🎯 **Para Gestores**
- [ ] Entender processo de solicitação de acesso
- [ ] Definir critérios para aprovação
- [ ] Estabelecer responsabilidades por área
- [ ] Treinar equipe nos novos processos

---

## 🚀 **PRÓXIMOS PASSOS**

1. **Validação do Fluxo:** Revisar com stakeholders
2. **Treinamento:** Capacitar equipes envolvidas
3. **Implementação Gradual:** Começar com um departamento piloto
4. **Monitoramento:** Acompanhar métricas e ajustar processos
5. **Documentação:** Manter procedimentos atualizados

---

*Documento criado em: Dezembro 2024*  
*Versão: 1.0*  
*Responsável: Equipe de Desenvolvimento*