# Documentação de Acessos - Módulo Jurídico

Esta documentação detalha os perfis de acesso, permissões e configurações para os usuários modelo do departamento Jurídico, servindo como referência para a criação de novos usuários em produção.

## 1. Estagiário Jurídico
- **Usuário Modelo:** `estagiario.juridico@sistema.com`
- **Perfil:** `ESTAGIARIO_JURIDICO`
- **Nível de Acesso:** `ADMIN` (Classificação cadastral no banco de dados)

### Permissões Mapeadas (Database)
As permissões abaixo estão associadas ao perfil `ESTAGIARIO_JURIDICO` na tabela `perfil_permissao`.

#### Geral
- `ROLE_USER`
- `ROLE_USER_ADMIN`
- `ROLE_USER_READ`
- `ROLE_USER_WRITE`
- `ROLE_USER_DELETE`
- `MENU_AJUDA`
- `MENU_PESSOAL`
- `MENU_PESSOAL_FAVORITOS`
- `MENU_PESSOAL_MEUS_PEDIDOS`
- `MENU_PESSOAL_MEUS_SERVICOS`
- `MENU_PESSOAL_RECOMENDACOES`

#### Jurídico
- `MENU_JURIDICO`
- `MENU_JURIDICO_DASHBOARD`
- `MENU_JURIDICO_DOCUMENTOS`
- `MENU_JURIDICO_PREVIDENCIARIO`
- `MENU_JURIDICO_PREVIDENCIARIO_LISTAR`
- `MENU_JURIDICO_PREVIDENCIARIO_NOVO`
- `MENU_JURIDICO_PROCESSOS`
- `MENU_JURIDICO_PROCESSOS_LISTAR`
- `MENU_JURIDICO_CLIENTES` (Acesso para cadastrar clientes para processos)
- `MENU_CLIENTES_NOVO` (Permissão de cadastro de clientes)
- `MENU_CLIENTES_DETALHES` (Visualizar detalhes do cliente)

---

## 2. Gerente Jurídico
- **Usuário Modelo:** `gerente.juridico@sistema.com`
- **Perfil:** `GERENTE_JURIDICO`
- **Nível de Acesso:** `ADMIN` (Classificação cadastral no banco de dados)

### Permissões Mapeadas (Database)
As permissões abaixo estão associadas ao perfil `GERENTE_JURIDICO` na tabela `perfil_permissao`.

#### Geral
- `ROLE_USER_ADMIN`
- `ROLE_USER_READ`
- `ROLE_USER_WRITE`
- `ROLE_USER_DELETE`
- `MENU_AJUDA`
- `MENU_PESSOAL`
- `MENU_PESSOAL_FAVORITOS`
- `MENU_PESSOAL_MEUS_PEDIDOS`
- `MENU_PESSOAL_MEUS_SERVICOS`
- `MENU_PESSOAL_RECOMENDACOES`

#### Jurídico
- `MENU_JURIDICO`
- `MENU_JURIDICO_DASHBOARD`
- `MENU_JURIDICO_CLIENTES` (Acesso ao módulo de Clientes)
- `MENU_JURIDICO_CONTRATOS`
- `MENU_JURIDICO_DOCUMENTOS`
- `MENU_JURIDICO_COMPLIANCE`
- `MENU_JURIDICO_WHATCHAT`
- `MENU_JURIDICO_AUDITORIA`
- `MENU_JURIDICO_AUDITORIA_INICIO`
- `MENU_JURIDICO_AUDITORIA_ACESSOS`
- `MENU_JURIDICO_AUDITORIA_ALTERACOES`
- `MENU_JURIDICO_AUDITORIA_EXPORTACOES`
- `MENU_JURIDICO_AUDITORIA_REVISOES`
- `MENU_JURIDICO_PREVIDENCIARIO`
- `MENU_JURIDICO_PREVIDENCIARIO_LISTAR`
- `MENU_JURIDICO_PREVIDENCIARIO_NOVO`
- `MENU_JURIDICO_PROCESSOS`
- `MENU_JURIDICO_PROCESSOS_LISTAR`
- `MENU_JURIDICO_PROCESSOS_NOVO`

## Observações para Produção
1. Ao criar um novo **Estagiário**, atribua o perfil `ESTAGIARIO_JURIDICO`.
2. Ao criar um novo **Gerente**, atribua o perfil `GERENTE_JURIDICO`.
3. Certifique-se de que a tabela `mapa_permissoes` contenha todas as chaves listadas acima.
4. O acesso ao módulo de **Clientes** para o Gerente Jurídico e Estagiário Jurídico é garantido pela permissão `MENU_JURIDICO_CLIENTES`, que foi mapeada no `ClienteController`. O Estagiário também recebeu `MENU_CLIENTES_NOVO` para cadastrar clientes necessários aos processos.
