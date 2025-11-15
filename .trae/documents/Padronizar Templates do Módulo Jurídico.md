## Diagnóstico Rápido

* Estrutura e componentes: todas as páginas usam `sidebar`, `topbar` e `footer` corretamente (ex.: templates/juridico/contratos.html:19–21, processos.html:19–21, compliance.html:19–21, documentos.html:19–21, contrato-detalhe.html:18–21).

* CSS/JS: seguem o padrão geral, mas há pequenas inconsistências de versão/origem (CDN vs local) e uso de inline styles.

* Semântica e UX: títulos com ícones e Bootstrap estão padronizados (ex.: contratos.html:25–27, processos.html:25–27, documentos.html:25–27, compliance.html:25–27).

* Páginas existentes do módulo: `index.html`, `contratos.html`, `contrato-detalhe.html`, `processos.html`, `compliance.html`, `documentos.html`. `dashboardJuridico.html` parece legado e fora do fluxo principal.

## Não Conformidades Detectadas

1. Versões de dependências

* Bootstrap via CDN em versões diferentes (`5.3.0` vs `5.3.3`) e não via `/css/bootstrap.min.css` recomendado (ex.: index.html:14; contratos.html:14).

* Font Awesome `6.0.0` em vez de `6.4.0` recomendado (ex.: contratos.html:13, processos.html:13, index.html:13).

* jQuery `3.7.1` em contrato-detalhe (contrato-detalhe.html:117) vs `3.7.0` nas demais.

1. Inline styles

* Bloco CSS extenso em `index.html` (index.html:15–89) fora do padrão de centralização em arquivo CSS.

1. Termos de status em Processos

* Filtros exibem valores que não existem na enum (`ATIVO`, `ARQUIVADO`) em vez de `EM_ANDAMENTO`, `SUSPENSO`, `ENCERRADO` (processos.html:37–40).

1. Layout alternativo legado

* `dashboardJuridico.html` usa um layout base diferente e não é referenciado pelo controller; desalinhado com o padrão atual.

1. Título dinâmico

* Padrão recomenda `th:text="${titulo}"` para o `<title>`; páginas usam títulos estáticos (ex.: contratos.html:7, processos.html:7).

## Plano de Padronização

1. Unificar dependências front-end

* Alterar todas as páginas do módulo para usar `/css/bootstrap.min.css` local e Font Awesome `6.4.0`.

* Padronizar jQuery em `3.7.0` e manter `notifications.js` e `sidebar.js` consistentes.

1. Migrar estilos inline

* Mover o bloco de CSS de `index.html` para um arquivo CSS comum (ex.: reutilizar `corporate.css` ou criar uma seção modular) e referenciar via `<link>`.

1. Corrigir filtros de Processos

* Atualizar opções de status para refletir a enum real e garantir que a busca/validação no front se alinhe às APIs.

1. Sanear \`

