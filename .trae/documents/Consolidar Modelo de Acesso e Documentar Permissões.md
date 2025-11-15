## Objetivo
Criar um documento conciso e consolidar o mecanismo de injeção de atributos globais de acesso (RBAC) para que toda a aplicação use uma fonte única e consistente.

## Entregáveis
1. Documento "Guia de Acesso e Permissões" com visão geral de papéis, níveis, áreas e quem pode fazer o quê.
2. Consolidação dos `@ModelAttribute` em um único `@ControllerAdvice` padronizado.
3. Ajustes nos templates e controllers para usar os atributos padronizados.

## Passos
### 1. Levantamento e Padronização
- Mapear todos os atributos hoje injetados: `usuarioLogado`, `isAdmin`, `isMaster`, `isGerencial`, `nivelAcesso`, `podeAcessar*`, `podeGerenciar*`.
- Adotar uma lista única de atributos globais (incluindo `podeAcessarCompras` e `podeAcessarProjetos` / `podeGerenciarProjetos`) com nomenclatura consistente.

### 2. Consolidação Técnica
- Unificar `GlobalControllerAdvice` e `UsuarioLogadoControllerAdvice` em um único advice.
- Garantir checagem segura do principal (`AnonymousAuthenticationToken`, `instanceof Usuario`).
- Injetar também `roles` (lista) e `permissions` (lista) para uso eventual dos templates.

### 3. Ajustes em Controllers
- Onde houver `@PreAuthorize` com expressão de bean, garantir que referenciem o advice unificado.
- Adicionar `@PreAuthorize` nas áreas que hoje dependem apenas do gating visual nos templates.

### 4. Ajustes em Templates
- Revisar `sidebar.html`, `topbar.html` e templates setoriais para usar os atributos padronizados e remover duplicações de lógica.

### 5. Documento
- Escrever o guia com: visão geral, fluxo de autenticação, matriz de acesso por área, exemplos de uso (`th:if`, `@PreAuthorize`), e boas práticas.

### 6. Validação
- Executar a aplicação, validar login com perfis distintos e conferir a exibição/ocultação correta das áreas e o bloqueio dos endpoints.

## Observações
- Nenhuma credencial será usada até sua confirmação para sair do modo de plano. Após aprovação, aplico as mudanças e entrego o documento junto aos ajustes de código.