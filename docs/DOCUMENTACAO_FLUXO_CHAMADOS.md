# Documentação do Sistema de Chamados - Portal CEO

## 📋 Visão Geral

O sistema de chamados do Portal CEO é um módulo de suporte técnico que permite a criação, gerenciamento e acompanhamento de solicitações de suporte. Este documento detalha as regras de negócio, permissões e fluxos do sistema.

## 👥 Perfis de Usuário e Permissões

### 1. ADMINISTRADOR
**Descrição:** Administrador do sistema com acesso total

**Permissões:**
- ✅ `CHAMADO_CRIAR` - Criar novos chamados
- ✅ `CHAMADO_VISUALIZAR` - Visualizar todos os chamados
- ✅ `CHAMADO_ATRIBUIR` - Atribuir chamados a técnicos
- ✅ `CHAMADO_INICIAR` - Iniciar atendimento de chamados
- ✅ `CHAMADO_RESOLVER` - Resolver chamados
- ✅ `CHAMADO_FECHAR` - Fechar chamados
- ✅ `CHAMADO_REABRIR` - Reabrir chamados fechados
- ✅ `CHAMADO_AVALIAR` - Avaliar chamados resolvidos
- ✅ `TECNICO_ATENDER_CHAMADOS` - Atender chamados como técnico
- ✅ `TECNICO_GERENCIAR_PROPRIOS_CHAMADOS` - Gerenciar próprios chamados
- ✅ `ADMIN_GERENCIAR_USUARIOS` - Gerenciar usuários do sistema

### 2. TÉCNICO
**Descrição:** Técnico de suporte responsável pelo atendimento

**Permissões:**
- ✅ `CHAMADO_CRIAR` - Criar novos chamados
- ✅ `CHAMADO_VISUALIZAR` - Visualizar chamados
- ✅ `CHAMADO_INICIAR` - Iniciar atendimento de chamados
- ✅ `CHAMADO_RESOLVER` - Resolver chamados
- ✅ `TECNICO_ATENDER_CHAMADOS` - Atender chamados como técnico
- ✅ `TECNICO_GERENCIAR_PROPRIOS_CHAMADOS` - Gerenciar próprios chamados

### 3. SUPERVISOR
**Descrição:** Supervisor de equipe com permissões de gestão

**Permissões:**
- ✅ `CHAMADO_CRIAR` - Criar novos chamados
- ✅ `CHAMADO_VISUALIZAR` - Visualizar todos os chamados
- ✅ `CHAMADO_ATRIBUIR` - Atribuir chamados a técnicos
- ✅ `CHAMADO_INICIAR` - Iniciar atendimento de chamados
- ✅ `CHAMADO_RESOLVER` - Resolver chamados
- ✅ `CHAMADO_FECHAR` - Fechar chamados
- ✅ `CHAMADO_REABRIR` - Reabrir chamados fechados
- ✅ `TECNICO_ATENDER_CHAMADOS` - Atender chamados como técnico

## 🎯 Regras de Negócio para Aceitar/Assumir Chamados

### Quem Pode Aceitar um Chamado?

Para que um usuário possa **aceitar/assumir** um chamado, ele deve possuir **pelo menos uma** das seguintes permissões:

1. **`TECNICO_ATENDER_CHAMADOS`** - Permissão específica para técnicos atenderem chamados
2. **`CHAMADO_INICIAR`** - Permissão para iniciar o atendimento de chamados
3. **`CHAMADO_ATRIBUIR`** - Permissão para atribuir chamados (inclui auto-atribuição)

### Perfis que Podem Aceitar Chamados:

| Perfil | Pode Aceitar? | Permissões Relevantes |
|--------|---------------|----------------------|
| **ADMINISTRADOR** | ✅ Sim | `TECNICO_ATENDER_CHAMADOS`, `CHAMADO_INICIAR`, `CHAMADO_ATRIBUIR` |
| **TÉCNICO** | ✅ Sim | `TECNICO_ATENDER_CHAMADOS`, `CHAMADO_INICIAR` |
| **SUPERVISOR** | ✅ Sim | `TECNICO_ATENDER_CHAMADOS`, `CHAMADO_INICIAR`, `CHAMADO_ATRIBUIR` |
| **USUÁRIO COMUM** | ❌ Não | Nenhuma permissão relevante |

## 🔄 Fluxo de Estados do Chamado

### Estados Possíveis:
1. **ABERTO** - Chamado criado, aguardando atendimento
2. **EM_ANDAMENTO** - Chamado sendo atendido por um técnico
3. **RESOLVIDO** - Chamado resolvido, aguardando validação
4. **FECHADO** - Chamado finalizado
5. **CANCELADO** - Chamado cancelado

### Transições de Estado:

```
ABERTO → EM_ANDAMENTO (Ação: "iniciar")
    ↓
EM_ANDAMENTO → RESOLVIDO (Ação: "resolver")
    ↓
RESOLVIDO → FECHADO (Ação: "fechar")
    ↓
FECHADO → ABERTO (Ação: "reabrir")

QUALQUER_ESTADO → CANCELADO (Ação: "cancelar")
```

## 📝 Processo de Aceitar/Assumir um Chamado

### 1. Pré-requisitos
- Usuário deve estar autenticado
- Usuário deve ter uma das permissões: `TECNICO_ATENDER_CHAMADOS`, `CHAMADO_INICIAR`, ou `CHAMADO_ATRIBUIR`
- Chamado deve estar no status **ABERTO**

### 2. Fluxo de Aceitação

#### Passo 1: Verificação de Permissões
```java
// O sistema verifica se o usuário pode gerenciar chamados
boolean podeAceitar = permissaoService.podeGerenciarChamados(usuario);
```

#### Passo 2: Ação de Iniciar Atendimento
```http
POST /suporte/api/chamados/{id}/status
Content-Type: application/json

{
    "acao": "iniciar",
    "statusDestino": "EM_ANDAMENTO",
    "tecnicoResponsavel": "Nome do Técnico",
    "observacoes": "Iniciando atendimento do chamado"
}
```

#### Passo 3: Validações do Sistema
1. **Chamado existe?** - Verifica se o ID do chamado é válido
2. **Transição válida?** - Confirma se pode ir de ABERTO para EM_ANDAMENTO
3. **Técnico informado?** - Valida se o nome do técnico foi fornecido
4. **Permissões?** - Confirma se o usuário tem permissão para a ação

#### Passo 4: Execução da Ação
```java
// No ChamadoService.iniciarAtendimento()
chamado.setStatus(StatusChamado.EM_ANDAMENTO);
chamado.setTecnicoResponsavel(tecnicoResponsavel);
chamado.setDataInicioAtendimento(LocalDateTime.now());
```

### 3. Resultado da Aceitação
- Status do chamado muda para **EM_ANDAMENTO**
- Campo `tecnicoResponsavel` é preenchido
- Campo `dataInicioAtendimento` é registrado
- Auditoria da ação é registrada

## 🚫 Cenários de Erro

### 1. Usuário Sem Permissão
**Erro:** "Usuário não tem permissão para gerenciar chamados"
**Causa:** Usuário não possui nenhuma das permissões necessárias

### 2. Chamado Não Encontrado
**Erro:** "Chamado não encontrado"
**Causa:** ID do chamado inválido ou inexistente

### 3. Transição Inválida
**Erro:** "Transição inválida de [STATUS_ATUAL] para EM_ANDAMENTO"
**Causa:** Chamado não está no status ABERTO

### 4. Técnico Não Informado
**Erro:** "Técnico responsável é obrigatório para iniciar atendimento"
**Causa:** Campo `tecnicoResponsavel` vazio ou nulo

## 🔍 Endpoints Relacionados

### 1. Verificar Permissões do Usuário
```http
GET /suporte/api/usuario/atual
```
**Resposta:**
```json
{
    "id": 123,
    "nome": "João Silva",
    "email": "joao@empresa.com",
    "podeAtenderChamados": true,
    "colaboradorId": 456,
    "cargo": "Técnico de TI",
    "departamento": "Tecnologia"
}
```

### 2. Listar Chamados Disponíveis
```http
GET /suporte/api/chamados
```

### 3. Aceitar/Iniciar Atendimento
```http
POST /suporte/api/chamados/{id}/status
```

### 4. Atribuição Automática
```http
POST /suporte/atribuir-automatico/{chamadoId}
```

## 📊 Monitoramento e Auditoria

### Eventos Auditados:
- ✅ Início de atendimento
- ✅ Mudanças de status
- ✅ Tentativas de operações não autorizadas
- ✅ Erros de operação

### Logs Importantes:
```
INFO - Atendimento iniciado para chamado CHM-2024-001 por João Silva
WARN - Tentativa de aceitar chamado sem permissão por usuário ID: 789
ERROR - Erro ao iniciar atendimento: Chamado não encontrado
```

## 🎯 Resumo das Regras de Aceitação

| Critério | Regra |
|----------|-------|
| **Quem pode aceitar** | Usuários com perfil ADMINISTRADOR, TÉCNICO ou SUPERVISOR |
| **Status necessário** | Chamado deve estar ABERTO |
| **Campos obrigatórios** | Técnico responsável deve ser informado |
| **Resultado** | Status muda para EM_ANDAMENTO |
| **Auditoria** | Todas as ações são registradas |

---

## 📞 Suporte

Para dúvidas sobre este fluxo, entre em contato com a equipe de desenvolvimento ou consulte os logs de auditoria do sistema.

**Última atualização:** Janeiro 2025
**Versão:** 1.0