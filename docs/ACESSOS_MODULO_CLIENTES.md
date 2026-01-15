# Módulo Clientes — Permissões e Botões

Este documento consolida todas as permissões (authorities) utilizadas no módulo de **Clientes** e como elas se relacionam com menus, telas e botões (especialmente editar e excluir).

## 1. Escopo do módulo Clientes

- Localização no menu: **Comercial → Clientes** (sidebar).
- Subáreas:
  - **Geral**: listagem e cadastro/edição de clientes.
  - **Contratos**: contratos do tipo CLIENTE.
  - **Histórico**: interações e pedidos por cliente.
  - **Avançado**: busca avançada e relatórios de clientes.
- Controle principal:
  - Authorities `MENU_CLIENTES*` para navegação.
  - Authorities `CLIENTE_EDITAR` e `CLIENTE_EXCLUIR` para ações sensíveis.
  - Perfis com `ROLE_ADMIN` ou `ROLE_MASTER` têm acesso ampliado em alguns fluxos.

---

## 2. Permissões de menu e navegação

### 2.1 Resumo das authorities de menu do módulo Clientes

Authorities principais mapeadas em código para o módulo:

- `MENU_CLIENTES`
- `MENU_CLIENTES_LISTAR`
- `MENU_CLIENTES_NOVO`
- `MENU_CLIENTES_DETALHES`
- `MENU_CLIENTES_CONTRATOS_LISTAR`
- `MENU_CLIENTES_HISTORICO_INTERACOES`
- `MENU_CLIENTES_HISTORICO_PEDIDOS`
- `MENU_CLIENTES_AVANCADO_BUSCA`
- `MENU_CLIENTES_AVANCADO_RELATORIOS`
- `MENU_JURIDICO_CLIENTES` (acesso ao módulo de Clientes a partir do Jurídico)

### 2.2 Tabelas de navegação por área

#### 2.2.1 Módulo e seção Comercial

| Área / Item                                         | Caminho principal  | Authorities relevantes                                                                                   | Efeito prático                                                                                                      |
|-----------------------------------------------------|--------------------|----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| Seção **Comercial** no sidebar                     | Sidebar            | `MENU_CLIENTES` ou `MENU_VENDAS` (qualquer um)                                                           | Exibe o título de seção “Comercial”.                                                                                |
| Agrupador **Clientes** no sidebar                  | Sidebar            | `MENU_CLIENTES`                                                                                          | Exibe o grupo “Clientes” e todos os submenus internos, respeitando as authorities de cada item-filho.              |

#### 2.2.2 Geral (Listagem e Cadastro)

| Tela / Item                                  | Caminhos                                                                                                      | Authorities necessárias em backend                                                                                       | Observações principais                                                                                                                                                           |
|----------------------------------------------|---------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Listagem de Clientes                         | `GET /clientes`<br>`GET /clientes/`<br>`GET /clientes/listar`                                                | `MENU_CLIENTES_LISTAR` **ou** `MENU_JURIDICO_CLIENTES` **ou** `ROLE_ADMIN` **ou** `ROLE_MASTER`                         | Controlado por `@PreAuthorize` em [ClienteController](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/controller/cliente/ClienteController.java#L37-L79). |
| Listagem de Clientes (API AJAX)              | `GET /clientes/api/listar`                                                                                   | Mesmo conjunto acima (`MENU_CLIENTES_LISTAR` ou `MENU_JURIDICO_CLIENTES` ou roles ADMIN/MASTER)                         | Alimenta a tabela da tela de listagem via JS.                                                                                             |
| Filtro rápido de clientes                    | `GET /clientes/filtro`                                                                                       | Mesmo conjunto acima                                                                                                     | Retorna listas filtradas por status.                                                                                                      |
| Seleção de clientes (componentes de select)  | `GET /clientes/api/select`                                                                                   | `MENU_CLIENTES_LISTAR`                                                                                                   | Usado por outras telas para buscar clientes ativos.                                                                                       |
| Cadastro de novo cliente (view)             | `GET /clientes/cadastro`                                                                                     | `MENU_CLIENTES_NOVO`                                                                                                     | Abre formulário de cadastro em [cadastro.html](file:///c:/Users/jasie/erp-corporativo/src/main/resources/templates/clientes/geral/cadastro.html).                                |
| Salvar novo cliente                          | `POST /clientes/salvar`                                                                                      | `MENU_CLIENTES_NOVO` **ou** `ROLE_ADMIN` **ou** `ROLE_MASTER`                                                            | Grava o cliente e redireciona para `/clientes`.                                                                                            |
| Detalhes do cliente                          | `GET /clientes/{id}/detalhes`                                                                                | `MENU_CLIENTES_DETALHES` **ou** `MENU_JURIDICO_CLIENTES` **ou** `ROLE_ADMIN` **ou** `ROLE_MASTER`                        | Visualiza ficha completa em [detalhes.html](file:///c:/Users/jasie/erp-corporativo/src/main/resources/templates/clientes/geral/detalhes.html).                                   |
| Edição de cliente (carregar formulário)      | `GET /clientes/{id}/editar`                                                                                  | `CLIENTE_EDITAR`                                                                                                         | Carrega dados do cliente para edição em [editar.html](file:///c:/Users/jasie/erp-corporativo/src/main/resources/templates/clientes/geral/editar.html).                            |
| Edição de cliente (salvar alterações)        | `POST /clientes/{id}/editar`                                                                                 | `CLIENTE_EDITAR`                                                                                                         | Atualiza os campos permitidos do cliente.                                                                                                  |
| Exclusão lógica de cliente (API)            | `POST /clientes/{id}/excluir`                                                                                | `CLIENTE_EXCLUIR`                                                                                                        | Além da authority, exige nível administrativo (`ehAdministrativo`) e confirmação de matrícula do próprio usuário.                                                               |

#### 2.2.3 Contratos de Clientes

| Tela / Item                                 | Caminhos                                                                                                  | Authorities necessárias                                                                               | Observações principais                                                                                                                                    |
|---------------------------------------------|-----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Listar contratos de clientes                | `GET /clientes/contratos`<br>`GET /clientes/contratos/listar`                                            | `MENU_CLIENTES_CONTRATOS_LISTAR`                                                                     | Lista contratos cujo tipo é `CLIENTE` em [listar.html](file:///c:/Users/jasie/erp-corporativo/src/main/resources/templates/clientes/contratos/listar.html). |
| Listagem de contratos (API)                | `GET /clientes/contratos/api/listar`                                                                      | `MENU_CLIENTES_CONTRATOS_LISTAR`                                                                     | Retorna contratos paginados e filtrados.                                                                                                                 |
| Detalhes do contrato (view)                | `GET /clientes/contratos/{id}/detalhes`                                                                   | `MENU_CLIENTES_CONTRATOS_LISTAR`                                                                     | Abre detalhes do contrato vinculado ao cliente.                                                                                                          |
| Detalhes do contrato (API)                 | `GET /clientes/contratos/api/{id}`                                                                        | `MENU_CLIENTES_CONTRATOS_LISTAR`                                                                     | Retorna os dados completos do contrato em JSON.                                                                                                          |

> Observação: o botão **“Novo Contrato”** da tela de contratos aponta para `/contratos/novo`, que usa as regras próprias do módulo de contratos gerais; essa permissão não é específica de Clientes.

#### 2.2.4 Histórico (Interações e Pedidos)

| Tela / Item                         | Caminhos                                                                                     | Authorities necessárias                  | Observações principais                                                                                                                       |
|-------------------------------------|----------------------------------------------------------------------------------------------|-------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| Histórico de Interações (view)     | `GET /clientes/historico/interacoes`                                                        | `MENU_CLIENTES_HISTORICO_INTERACOES`      | Exibe indicadores e tabela com interações de clientes em [interacoes.html](file:///c:/Users/jasie/erp-corporativo/src/main/resources/templates/clientes/historico/interacoes.html). |
| Histórico de Interações (API)      | `GET /clientes/historico/api/interacoes`                                                    | `MENU_CLIENTES_HISTORICO_INTERACOES`      | Fornece dados paginados para a tabela.                                                                                                      |
| Histórico de Pedidos (view)        | `GET /clientes/historico/pedidos`                                                           | `MENU_CLIENTES_HISTORICO_PEDIDOS`         | Exibe indicadores de pedidos associados a clientes em [pedidos.html](file:///c:/Users/jasie/erp-corporativo/src/main/resources/templates/clientes/historico/pedidos.html).        |
| Histórico de Pedidos (API)         | `GET /clientes/historico/api/pedidos`                                                       | `MENU_CLIENTES_HISTORICO_PEDIDOS`         | Lista pedidos paginados com valores e status.                                                                                               |

#### 2.2.5 Avançado (Busca e Relatórios)

| Tela / Item                         | Caminhos                                                                                   | Authorities necessárias                 | Observações principais                                                                                                                        |
|-------------------------------------|--------------------------------------------------------------------------------------------|------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| Busca Avançada de Clientes (view)   | `GET /clientes/avancado/busca`                                                             | `MENU_CLIENTES_AVANCADO_BUSCA`          | Página com filtros avançados em [busca.html](file:///c:/Users/jasie/erp-corporativo/src/main/resources/templates/clientes/avancado/busca.html).                                     |
| Busca Avançada de Clientes (API)    | `GET /clientes/avancado/api/busca`                                                         | `MENU_CLIENTES_AVANCADO_BUSCA`          | Retorna clientes filtrados com múltiplos critérios (status, tipo, VIP, ativo, origem etc.).                                                  |
| Relatórios de Clientes (view)       | `GET /clientes/avancado/relatorios`                                                        | `MENU_CLIENTES_AVANCADO_RELATORIOS`     | Exibe métricas, contratos e pedidos agregados em [relatorios.html](file:///c:/Users/jasie/erp-corporativo/src/main/resources/templates/clientes/avancado/relatorios.html).        |

#### 2.2.6 Acesso ao módulo Clientes a partir do Jurídico

| Cenário                           | Caminhos                         | Authorities consideradas                                      | Efeito                                                                                             |
|-----------------------------------|----------------------------------|----------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| Acesso Jurídico a Clientes        | `/clientes` e `/clientes/{id}`  | `MENU_JURIDICO_CLIENTES`                                     | Permite que perfis jurídicos acessem listagem/detalhes sem `MENU_CLIENTES_LISTAR`/`_DETALHES`.    |

---

## 3. Permissões de ações e botões

### 3.1 Listagem de Clientes (`clientes/geral/listar.html`)

Principais botões/links por linha e na toolbar:

- Botão **“Novo Cliente”** (toolbar)
  - Comportamento: link visível para todos que veem a página.
  - Backend: acesso ao formulário `/clientes/cadastro` exige `MENU_CLIENTES_NOVO`.

- Botão **“Detalhes”** (ícone de olho por linha)
  - Comportamento: sempre renderizado na tabela.
  - Backend: rota `/clientes/{id}/detalhes` exige `MENU_CLIENTES_DETALHES` ou `MENU_JURIDICO_CLIENTES` ou roles `ADMIN`/`MASTER`.

- Botão **“Processos Jurídicos”** (ícone de martelo)
  - Comportamento: só é exibido quando `podeAcessarJuridico` é verdadeiro (flag global populada por `GlobalControllerAdvice`).
  - Backend: rota `/juridico/processos/cliente/{id}` segue regras do módulo Jurídico (não específicas de Clientes).

- Botão **“Editar”** (ícone de lápis por linha)
  - Visibilidade no front: controlada pela flag `podeEditar` enviada pelo controller.
    - `podeEditar = true` quando o usuário possui `CLIENTE_EDITAR` **ou** roles `ROLE_MASTER` ou `ROLE_ADMIN`.
  - Backend:
    - `GET /clientes/{id}/editar`
    - `POST /clientes/{id}/editar`
    - Ambos exigem a authority `CLIENTE_EDITAR`.

- Botão **“Excluir”** (ícone de usuário com “x” por linha)
  - Visibilidade no front: controlada pela flag `podeExcluir`.
    - `podeExcluir = true` quando o usuário possui `CLIENTE_EXCLUIR` **ou** roles `ROLE_MASTER` ou `ROLE_ADMIN`.
  - Backend:
    - Chama `POST /clientes/{id}/excluir` via SweetAlert, enviando a matrícula do usuário no header `X-Matricula`.
    - Exige a authority `CLIENTE_EXCLUIR`.
    - Validações adicionais:
      - Usuário autenticado deve ter nível de acesso administrativo (`ehAdministrativo()`).
      - A matrícula informada deve coincidir com a matrícula do usuário autenticado.
      - Exclusão é lógica (inativação) via `clienteService.excluirLogicamente`.

### 3.2 Tela de Detalhes do Cliente (`clientes/geral/detalhes.html`)

Botões principais:

- Botão **“Editar”**
  - Sempre visível na view.
  - Ao clicar, chama `/clientes/{id}/editar`, que exige `CLIENTE_EDITAR`.

- Botão **“Processos Jurídicos”**
  - Sempre exibido na tela de detalhes.
  - Aponta para `/juridico/processos/cliente/{cliente.id}`; permissões controladas pelo módulo Jurídico e pela flag `podeAcessarJuridico`.

- Botão **“Excluir”**
  - Renderizado apenas quando `podeExcluir` é verdadeiro (mesma regra de `CLIENTE_EXCLUIR` + roles ADMIN/MASTER herdada da listagem).
  - Usa o mesmo fluxo de confirmação e endpoint `POST /clientes/{id}/excluir`.

- Botão **“Voltar”**
  - Navega de volta para `/clientes`; o acesso continua condicionado a `MENU_CLIENTES_LISTAR` / `MENU_JURIDICO_CLIENTES` / roles.

### 3.3 Cadastro e Edição (`clientes/geral/cadastro.html` e `clientes/geral/editar.html`)

Botões relevantes:

- Botão **“Salvar”** (cadastro)
  - Formulário envia para `POST /clientes/salvar`.
  - Exige `MENU_CLIENTES_NOVO` ou roles `ADMIN`/`MASTER`.

- Botão **“Salvar Alterações”** (edição)
  - Formulário envia para `POST /clientes/{id}/editar`.
  - Exige `CLIENTE_EDITAR`.

- Botões **“Cancelar”/“Voltar”**
  - Apenas redirecionam para `/clientes`; não possuem verificação extra além da proteção da própria listagem.

### 3.4 Contratos, Histórico e Avançado

- Em **Contratos de Clientes**:
  - A tela de listagem e todas as ações de leitura (filtros, paginação, detalhes) exigem `MENU_CLIENTES_CONTRATOS_LISTAR`.
  - O botão **“Novo Contrato”** é genérico do módulo de contratos e pode depender de permissões adicionais em outros controllers.

- Em **Histórico de Interações**:
  - Todos os filtros, paginação e links para “Ver cliente” dependem do acesso à página e APIs protegidas por `MENU_CLIENTES_HISTORICO_INTERACOES`.

- Em **Histórico de Pedidos**:
  - Filtros, paginação, métricas e listagem utilizam `MENU_CLIENTES_HISTORICO_PEDIDOS`.

- Em **Busca Avançada**:
  - Filtros, paginação e visualização dos resultados dependem de `MENU_CLIENTES_AVANCADO_BUSCA`.

- Em **Relatórios de Clientes**:
  - Carregamento de métricas e indicadores exige `MENU_CLIENTES_AVANCADO_RELATORIOS`.

---

## 4. Authorities de ação do módulo Clientes (resumo)

Para facilitar a concessão de acessos finos (botões e operações críticas), seguem as authorities de ação diretamente ligadas ao módulo:

- `CLIENTE_EDITAR`
  - Permite editar dados de clientes.
  - Habilita:
    - Botão “Editar” na listagem de clientes.
    - Acesso às rotas `/clientes/{id}/editar` (GET e POST).

- `CLIENTE_EXCLUIR`
  - Permite exclusão lógica (inativação) de clientes.
  - Habilita:
    - Botão “Excluir” na listagem e na tela de detalhes.
    - Endpoint `POST /clientes/{id}/excluir`, sujeito também a:
      - Nível de acesso administrativo.
      - Confirmação de matrícula do usuário autenticado.

Com estas informações é possível montar perfis que apenas visualizam clientes, perfis que podem cadastrar/editar, e perfis administrativos que também podem excluir, contratualizar e analisar indicadores avançados do módulo de Clientes.

