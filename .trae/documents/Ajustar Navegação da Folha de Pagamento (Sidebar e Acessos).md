## Situação Atual
- Sidebar (RH → Folha de Pagamento) contém 4 itens: `Gerar`, `Holerite`, `Descontos`, `Relatórios` (src/main/resources/templates/components/sidebar.html:154–157).
- Demais páginas são acessadas por botões/fluxo dentro das telas:
  - `index` acessada por botões de "Voltar" em `holerite.html:191` e `holerite-index.html:52`, e por rotas diretas.
  - `listar` acessada via botão em `index.html:93`.
  - `visualizar/{id}` acessada via botão em `index.html:67` e redirects de processar/fechar (controller).
  - `holerite/{id}` acessada via ação em `visualizar.html:93`, `holerite.html:414`.
  - `holerites-colaborador` acessada a partir de `holerite-index` (monta URL) em `holerite-index.html:74`.
- Link órfão: `gerar.html` referencia `/rh/folha-pagamento/historico` (gerar.html:47) sem endpoint/template.

## Objetivo
- Tornar as principais páginas acessíveis diretamente pelo sidebar e eliminar link quebrado.

## Proposta de Ajustes
1. Adicionar ao sidebar:
   - `Folha (Início)` → `/rh/folha-pagamento`.
   - `Listar Folhas` → `/rh/folha-pagamento/listar`.
2. Manter `Gerar`, `Holerite` (abre `holerite-index`), `Descontos`, `Relatórios` como estão.
3. Não adicionar `visualizar/{id}` e `holerite/{id}` ao sidebar (são contextuais por ID).
4. Corrigir `gerar.html` removendo ou apontando `Histórico` para `/rh/folha-pagamento/listar`.

## Validação
- Verificar visibilidade via `podeGerenciarRH/podeAcessarRH`.
- Acessar cada item do sidebar e confirmar renderização correta.

## Entregáveis
- Sidebar atualizado com 6 entradas para Folha de Pagamento.
- Link de histórico corrigido em `gerar.html`.