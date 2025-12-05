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
