# Análise Completa do Módulo RH - ERP Corporativo

## 📋 Visão Geral

Este documento apresenta uma análise detalhada do módulo de Recursos Humanos (RH) do ERP Corporativo, incluindo lógica de negócio, regras de acesso, níveis de permissão e fluxo completo desde o login do usuário até as funcionalidades específicas.

---

## 🔐 Sistema de Autenticação e Autorização

### Fluxo de Login

1. **LoginController** (`LoginController.java`)
   - Endpoint: `/` ou `/login`
   - Renderiza a página de login
   - Integrado com Spring Security

2. **Autenticação**
   - Utiliza Spring Security com configuração customizada
   - Senhas criptografadas com BCrypt
   - Tratamento de falhas de autenticação personalizado

3. **Pós-Login**
   - Redirecionamento para `/dashboard`
   - Carregamento do contexto do usuário
   - Definição de permissões e níveis de acesso

### Níveis de Acesso (NivelAcesso.java)

O sistema possui uma hierarquia bem definida de níveis de acesso:

```java
public enum NivelAcesso {
    MASTER("Master"),
    ADMIN("Administrador"),
    GERENTE("Gerente"),
    COORDENADOR("Coordenador"),
    SUPERVISOR("Supervisor"),
    ANALISTA("Analista"),
    ASSISTENTE("Assistente"),
    USER("Usuário"),
    VISITANTE("Visitante")
}
```

#### Hierarquia de Autoridade
- **MASTER**: Nível mais alto, pode gerenciar tudo
- **ADMIN**: Pode gerenciar usuários e configurações
- **GERENTE**: Pode gerenciar RH e equipes
- **COORDENADOR**: Acesso limitado a coordenação
- **SUPERVISOR**: Supervisão de equipes
- **ANALISTA**: Acesso analítico
- **ASSISTENTE**: Suporte operacional
- **USER**: Usuário básico
- **VISITANTE**: Acesso mínimo

---

## 🏢 Controle de Acesso ao Módulo RH

### Quem Pode Acessar o Módulo RH?

#### 1. **Verificação Global (GlobalControllerAdvice.java)**

```java
@ModelAttribute("podeAcessarRH")
public boolean podeAcessarRH() {
    return isRH() || podeGerenciarRH() || isMaster() || isAdmin();
}

@ModelAttribute("podeGerenciarRH")
public boolean podeGerenciarRH() {
    Usuario usuario = usuarioLogado();
    return usuario != null && usuario.getNivelAcesso().podeGerenciarRH();
}
```

#### 2. **Regras de Acesso por Nível**

| Nível de Acesso | Pode Acessar RH | Pode Gerenciar RH | Observações |
|----------------|----------------|-------------------|-------------|
| **MASTER** | ✅ | ✅ | Acesso total |
| **ADMIN** | ✅ | ✅ | Acesso administrativo |
| **GERENTE** | ✅ | ✅ | Gestão de RH |
| **COORDENADOR** | ✅ | ❌ | Apenas visualização |
| **SUPERVISOR** | ✅ | ❌ | Acesso limitado |
| **ANALISTA** | ❌ | ❌ | Sem acesso |
| **ASSISTENTE** | ❌ | ❌ | Sem acesso |
| **USER** | ❌ | ❌ | Sem acesso |
| **VISITANTE** | ❌ | ❌ | Sem acesso |

#### 3. **Verificação por Funcionalidade (PermissaoUsuarioService.java)**

```java
public boolean podeAcessarFuncionalidade(Long usuarioId, String funcionalidade) {
    // ...
    return switch (funcionalidade.toLowerCase()) {
        case "rh", "recursos_humanos" -> nivel.podeGerenciarRH();
        // ...
    };
}
```

---

## 👥 Funcionalidade de Promoção de Colaboradores

### Quem Pode Promover Colaboradores?

#### Regras de Negócio para Promoção

1. **Níveis Autorizados**:
   - **MASTER**: Pode promover qualquer colaborador
   - **ADMIN**: Pode promover qualquer colaborador
   - **GERENTE**: Pode promover colaboradores de níveis inferiores

2. **Validações Implementadas (ColaboradorController.java)**:

```java
@PostMapping("/promover/{id}")
public String promover(@PathVariable Long id, 
                      @RequestParam Long novoCargoId,
                      @RequestParam BigDecimal novoSalario,
                      @RequestParam(required = false) String descricao) {
    
    // Validação 1: Cargo deve ser diferente
    if (colaborador.getCargo().getId().equals(novoCargoId)) {
        // Erro: "O novo cargo deve ser diferente do cargo atual"
    }
    
    // Validação 2: Salário deve ser maior
    if (novoSalario.compareTo(colaborador.getSalario()) <= 0) {
        // Erro: "O novo salário deve ser maior que o salário atual"
    }
    
    // Validação 3: Cargo deve existir
    var novoCargo = cargoService.findById(novoCargoId);
    if (novoCargo == null) {
        // Erro: "Cargo não encontrado"
    }
}
```

3. **Processo de Promoção**:
   - Registra no histórico do colaborador
   - Atualiza cargo e salário
   - Salva as alterações
   - Exibe mensagem de sucesso

### Histórico de Promoções

#### Entidade HistoricoColaborador
- Registra todas as mudanças de cargo e salário
- Inclui data, descrição e valores anteriores/novos
- Permite rastreabilidade completa

---

## 🏗️ Estrutura do Módulo RH

### Controllers

#### 1. **RhController** - Dashboard e Navegação
- `/rh` - Dashboard principal
- `/rh/colaboradores` - Listagem
- `/rh/folha-pagamento` - Folha de pagamento
- `/rh/beneficios` - Gestão de benefícios
- `/rh/ponto-escalas` - Controle de ponto

#### 2. **ColaboradorController** - Gestão de Colaboradores
- `/rh/colaboradores/novo` - Cadastro
- `/rh/colaboradores/editar/{id}` - Edição
- `/rh/colaboradores/ficha/{id}` - Ficha detalhada
- `/rh/colaboradores/promover/{id}` - **Promoção**
- `/rh/colaboradores/desativar/{id}` - Desativação

#### 3. **PlanoSaudeController** - Benefícios de Saúde
- `/rh/beneficios/plano-saude` - Gestão de planos

### Services (Lógica de Negócio)

#### 1. **ColaboradorService**
- Validações de negócio
- Associações com cargos/departamentos
- Cálculo de tempo na empresa
- **Registro de promoções**

#### 2. **CargoService**
- Gestão de cargos
- Hierarquias organizacionais

#### 3. **BeneficioService**
- Gestão de benefícios
- Associações colaborador-benefício

### Repositories (Acesso a Dados)

- **ColaboradorRepository**
- **CargoRepository**
- **BeneficioRepository**
- **HoleriteRepository**
- **HistoricoColaboradorRepository**

---

## 📊 Regras de Negócio Detalhadas

### 1. **Gestão de Colaboradores**

#### Cadastro
- Validação de CPF único
- Associação obrigatória com cargo e departamento
- Definição de supervisor (opcional)
- Associação com benefícios

#### Edição
- Apenas usuários autorizados podem editar
- Histórico de alterações mantido
- Validações de integridade

#### Desativação
- Colaborador não é excluído, apenas desativado
- Mantém histórico completo
- Data de desligamento registrada

### 2. **Sistema de Promoções**

#### Pré-requisitos
- Usuário deve ter permissão `podeGerenciarRH()`
- Colaborador deve estar ativo
- Novo cargo deve existir no sistema

#### Validações
1. **Cargo diferente**: Novo cargo ≠ cargo atual
2. **Salário maior**: Novo salário > salário atual
3. **Cargo válido**: Cargo deve existir na base

#### Processo
1. Validação das regras
2. Registro no histórico
3. Atualização dos dados do colaborador
4. Confirmação da operação

### 3. **Controle de Acesso Granular**

#### Por Funcionalidade
```java
// Exemplo de verificação
if (!podeGerenciarRH()) {
    return "redirect:/acesso-negado";
}
```

#### Por Nível Hierárquico
```java
// Verificação de autoridade
if (!usuarioEditor.getNivelAcesso().temAutoridadeSobre(usuarioAlvo.getNivelAcesso())) {
    return false;
}
```

---

## 🔄 Fluxo Completo: Login → Promoção

### 1. **Login do Usuário**
```
Usuário acessa /login
↓
Spring Security valida credenciais
↓
GlobalControllerAdvice carrega contexto
↓
Redirecionamento para /dashboard
```

### 2. **Acesso ao Módulo RH**
```
Usuário clica em "RH" no menu
↓
Verificação: podeAcessarRH()
↓
Se autorizado: /rh (dashboard)
Se negado: Acesso negado
```

### 3. **Navegação para Colaboradores**
```
/rh → "Colaboradores"
↓
Verificação: podeGerenciarRH()
↓
/rh/colaboradores (listagem)
```

### 4. **Acesso à Ficha do Colaborador**
```
Clique em "Ver Ficha"
↓
/rh/colaboradores/ficha/{id}
↓
Carregamento dos dados completos
↓
Exibição do botão "Promover" (se autorizado)
```

### 5. **Processo de Promoção**
```
Clique em "Promover"
↓
Modal com formulário
↓
Preenchimento: novo cargo, salário, descrição
↓
Submissão para /rh/colaboradores/promover/{id}
↓
Validações de negócio
↓
Se válido: Atualização + Histórico
Se inválido: Mensagem de erro
↓
Redirecionamento com feedback
```

---

## 🛡️ Segurança e Auditoria

### Logs de Auditoria
- Todas as promoções são registradas
- Histórico completo mantido
- Rastreabilidade de alterações

### Proteções Implementadas
- Validação de autorização em cada endpoint
- Sanitização de dados de entrada
- Prevenção de escalação de privilégios
- Proteção contra usuários protegidos (MASTER)

### Tratamento de Erros
- Mensagens de erro específicas
- Redirecionamentos seguros
- Logs de tentativas não autorizadas

---

## 📈 Status Atual de Implementação

### ✅ **Funcionalidades Completas**
- Sistema de autenticação
- Controle de acesso por níveis
- Gestão básica de colaboradores
- **Funcionalidade de promoção**
- Histórico de alterações
- Validações de negócio

### 🟡 **Funcionalidades Parciais**
- Folha de pagamento (estrutura criada)
- Relatórios (templates básicos)
- Benefícios (CRUD básico)

### ❌ **Funcionalidades Pendentes**
- Controle de ponto avançado
- Avaliações de desempenho
- Gestão de documentos
- Integração com sistemas externos

---

## 🎯 Conclusões e Recomendações

### Pontos Fortes
1. **Arquitetura sólida** com separação clara de responsabilidades
2. **Sistema de segurança robusto** com múltiplas camadas
3. **Funcionalidade de promoção completa** e bem validada
4. **Rastreabilidade total** através do histórico
5. **Interface intuitiva** para operações de RH

### Áreas de Melhoria
1. **Implementar logs de auditoria** mais detalhados
2. **Adicionar notificações** para promoções
3. **Criar relatórios** de movimentação de pessoal
4. **Implementar aprovação** em múltiplas etapas
5. **Adicionar validações** de orçamento para promoções

### Próximos Passos
1. Completar funcionalidades de folha de pagamento
2. Implementar sistema de aprovações
3. Adicionar relatórios gerenciais
4. Criar dashboard de métricas de RH
5. Implementar integração com sistemas externos

---

**Documento gerado em:** " + new Date().toLocaleDateString('pt-BR') + "
**Versão:** 1.0
**Autor:** Análise Técnica do Sistema
**Módulo:** Recursos Humanos (RH) - ERP Corporativo