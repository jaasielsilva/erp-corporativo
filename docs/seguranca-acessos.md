# Onboarding Seguro de Colaborador

Este documento padroniza o passo a passo para criação de usuários e concessão de acessos mínimos, garantindo segurança e conformidade.

## Fluxograma (Passo a Passo)

```mermaid
flowchart TD
    A[Preparação] --> B[Cadastro]
    B --> C[Permissões]
    C --> D[Verificação]
    D --> E[Revisão Periódica]

    subgraph A1 [Preparação]
      A1a[Validar dados: nome, email corporativo, CPF] --> A1b[Identificar cargo e departamento]
      A1b --> A1c[Definir módulos necessários]
      A1c --> A1d[Definir nível de acesso adequado]
    end

    subgraph B1 [Cadastro]
      B1a[Acessar: Administração → Gestão de Acesso → Usuários → Cadastro] --> B1b[Preencher campos obrigatórios]
      B1b --> B1c[Definir senha conforme política]
      B1c --> B1d[Selecionar perfil inicial compatível (USUARIO, TECNICO, FINANCEIRO, JURIDICO)]
    end

    subgraph C1 [Permissões]
      C1a[Conceder apenas MENU_* necessários] --> C1b[Conceder apenas operações específicas]
      C1b --> C1c[Evitar WRITE/DELETE fora do escopo]
    end

    subgraph D1 [Verificação]
      D1a[Testar login do usuário] --> D1b[Validar visibilidade dos menus]
      D1b --> D1c[Tentar operações fora do escopo → negar]
      D1c --> D1d[Registrar aceites de termos e auditorias]
    end

    subgraph E1 [Revisão Periódica]
      E1a[Revisar perfis em mudanças de função/departamento] --> E1b[Monitorar acessos indevidos]
      E1b --> E1c[Ajustar perfis e remover excessos]
    end
```

## Detalhamento dos Passos

- Preparação
  - Validar dados do colaborador: nome, email corporativo, CPF, cargo e departamento.
  - Definir módulos necessários e o nível de acesso adequado, evitando níveis elevados quando não estritamente necessários.

- Cadastro
  - Acessar: Administração → Gestão de Acesso → Usuários → Cadastro.
  - Preencher todos os campos obrigatórios e definir senha conforme a política de segurança vigente.
  - Selecionar um perfil inicial compatível com o cargo (ex.: USUARIO, TECNICO, FINANCEIRO, JURIDICO).

- Permissões
  - Conceder apenas MENU_* para menus realmente necessários (visibilidade de seções).
  - Conceder apenas operações específicas (ROLE_, CHAMADO_, FINANCEIRO_*), evitando WRITE/DELETE se não forem parte do escopo do cargo.

- Verificação
  - Testar login com o usuário recém-criado.
  - Validar a visibilidade dos menus e confirmar que operações fora do escopo são negadas.
  - Registrar aceites de termos e garantir que auditorias de acesso estejam ativas conforme compliance.

- Revisão periódica
  - Revisar perfis e permissões quando houver mudanças de função ou departamento.
  - Monitorar tentativas de acesso indevidas e ajustar perfis removendo excessos.

