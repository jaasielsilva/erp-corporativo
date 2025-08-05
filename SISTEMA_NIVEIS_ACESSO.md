# Sistema de Níveis de Acesso - Portal CEO

## Visão Geral

O sistema implementa um controle de acesso robusto baseado em níveis hierárquicos, garantindo segurança e organização das permissões.

## Níveis de Acesso Implementados

### 1. MASTER (Nível 10)
- **Descrição**: Nível máximo do sistema, imutável
- **Características**:
  - Acesso total a todas as funcionalidades
  - Não pode ser editado nem excluído
  - Usuário protegido: `master@sistema.com`
  - Senha: `master123`

### 2. ADMIN (Nível 9)
- **Descrição**: Administrador do sistema
- **Características**:
  - Gerenciamento completo de usuários
  - Acesso a configurações do sistema
  - Usuário protegido: `admin@teste.com`
  - Senha: `12345`

### 3. GERENTE (Nível 8)
- **Descrição**: Gerência executiva
- **Características**:
  - Acesso a relatórios financeiros
  - Gerenciamento de RH
  - Aprovação de processos
  - Usuário exemplo: `rh@empresa.com`
  - Senha: `rh123`

### 4. COORDENADOR (Nível 7)
- **Descrição**: Coordenação de equipes
- **Características**:
  - Supervisão de equipes
  - Relatórios departamentais
  - Usuário exemplo: `coordenador@empresa.com`
  - Senha: `coord123`

### 5. SUPERVISOR (Nível 6)
- **Descrição**: Supervisão operacional
- **Características**:
  - Supervisão direta de colaboradores
  - Aprovações operacionais

### 6. ANALISTA (Nível 5)
- **Descrição**: Funções analíticas e técnicas
- **Características**:
  - Análise de dados
  - Relatórios técnicos

### 7. OPERACIONAL (Nível 4)
- **Descrição**: Operações do dia a dia
- **Características**:
  - Acesso limitado às funcionalidades básicas
  - Usuário exemplo: `operacional@empresa.com`
  - Senha: `op123`

### 8. ESTAGIARIO (Nível 3)
- **Descrição**: Acesso para estagiários
- **Características**:
  - Acesso supervisionado
  - Funcionalidades limitadas

### 9. TERCEIRIZADO (Nível 2)
- **Descrição**: Colaboradores terceirizados
- **Características**:
  - Acesso restrito
  - Apenas funcionalidades específicas

### 10. CONSULTOR (Nível 2)
- **Descrição**: Consultores externos
- **Características**:
  - Acesso temporário
  - Funcionalidades específicas do projeto

### 11. VISITANTE (Nível 1)
- **Descrição**: Acesso mínimo
- **Características**:
  - Apenas visualização
  - Acesso muito limitado

## Permissões por Nível

### Gerenciamento de Usuários
- **MASTER/ADMIN**: Controle total
- **GERENTE**: Pode gerenciar níveis inferiores
- **COORDENADOR/SUPERVISOR**: Pode visualizar equipe
- **Demais**: Apenas próprio perfil

### Módulo RH
- **MASTER/ADMIN/GERENTE**: Acesso completo
- **COORDENADOR/SUPERVISOR**: Acesso limitado
- **Demais**: Apenas consultas pessoais

### Financeiro
- **MASTER/ADMIN**: Acesso total
- **GERENTE**: Relatórios e aprovações
- **Demais**: Sem acesso

### Relatórios
- **MASTER/ADMIN**: Todos os relatórios
- **GERENTE**: Relatórios gerenciais
- **COORDENADOR/SUPERVISOR**: Relatórios da equipe
- **Demais**: Relatórios pessoais

## Sidebar Dinâmico

O menu lateral se adapta automaticamente baseado no nível de acesso:

- **Níveis Gerenciais** (MASTER, ADMIN, GERENTE, COORDENADOR, SUPERVISOR):
  - Módulo RH completo
  - Relatórios gerenciais
  - Configurações avançadas

- **Níveis Operacionais** (ANALISTA, OPERACIONAL, ESTAGIARIO):
  - Funcionalidades básicas
  - Chat e suporte
  - Documentos pessoais

- **Níveis Externos** (TERCEIRIZADO, CONSULTOR, VISITANTE):
  - Acesso muito restrito
  - Apenas funcionalidades essenciais

## Proteções Implementadas

### Usuários Protegidos
1. **master@sistema.com**: Nunca pode ser alterado ou excluído
2. **admin@teste.com**: Protegido contra alterações críticas

### Validações de Segurança
- Usuários não podem alterar próprio nível de acesso
- Apenas níveis superiores podem gerenciar inferiores
- Proteção contra autoexclusão
- Validação de último administrador

### Hierarquia de Autoridade
- Cada nível pode gerenciar apenas níveis inferiores
- MASTER tem autoridade sobre todos
- Validação automática de permissões

## Implementação Técnica

### Arquivos Principais
1. `NivelAcesso.java` - Enum com todos os níveis
2. `PermissaoUsuarioService.java` - Lógica de permissões
3. `UsuarioService.java` - Validações de segurança
4. `GlobalControllerAdvice.java` - Atributos para frontend
5. `sidebar.html` - Menu dinâmico

### Métodos de Validação
- `temAutoridadeSobre()` - Verifica hierarquia
- `podeGerenciarUsuarios()` - Permissão de gestão
- `podeAcessarFinanceiro()` - Acesso financeiro
- `podeGerenciarRH()` - Gestão de RH
- `ehProtegido()` - Usuários protegidos

## Expansibilidade

O sistema foi projetado para ser facilmente expansível:
- Novos níveis podem ser adicionados ao enum
- Permissões específicas podem ser criadas
- Validações customizadas podem ser implementadas
- Interface se adapta automaticamente

## Segurança

- Criptografia de senhas com BCrypt
- Validação de sessão
- Proteção contra escalação de privilégios
- Logs de auditoria (recomendado implementar)
- Timeout de sessão (configurável)

Este sistema garante um controle de acesso profissional, seguro e escalável para o Portal CEO.