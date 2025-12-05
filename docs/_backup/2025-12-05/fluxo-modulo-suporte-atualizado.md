# Fluxo do Módulo de Suporte - Documentação Atualizada

## Visão Geral

Este documento descreve o fluxo real e atualizado do módulo de suporte do sistema ERP, baseado na análise do código fonte e correção de inconsistências identificadas.

## Fluxograma Visual

O fluxograma completo está disponível em: `fluxograma-modulo-suporte.svg`

## Fluxo Principal dos Chamados

### 1. Acesso ao Sistema
- **URL Principal**: `/suporte`
- **Controller**: `SuporteController.java`
- **Método**: `dashboard()` - Exibe a página principal com chamados recentes

### 2. Criação de Novo Chamado

#### 2.1 Formulário de Criação
- **URL**: `/suporte/novo`
- **Método**: `novoFormulario()`
- **Campos obrigatórios**:
  - Assunto
  - Descrição
  - Prioridade (URGENTE, ALTA, MEDIA, BAIXA)
  - Categoria (opcional)

#### 2.2 Processamento da Criação
- **URL**: `POST /suporte/chamados`
- **Método**: `criarChamado()`
- **Processo**:
  1. Validação dos dados
  2. Geração do número único (formato: CH + timestamp)
  3. Cálculo do SLA baseado na prioridade
  4. Status inicial: `ABERTO`
  5. Técnico: `Não atribuído`

### 3. Estados e Transições
