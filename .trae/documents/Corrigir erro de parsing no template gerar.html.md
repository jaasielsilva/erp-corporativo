## Diagnóstico
- Erro ao abrir `GET /rh/folha-pagamento/gerar`: "An error happened during template parsing" indica falha de sintaxe em Thymeleaf.
- Causa provável: atributo `th:classappend` com múltiplas expressões separadas em vez de uma única expressão.
  - Local: `src/main/resources/templates/rh/folha-pagamento/gerar.html:141-145`.
  - Thymeleaf exige uma expressão única; o formato atual mistura `${...}` em sequência, o que quebra o parser.
- Itens secundários (não causam parsing, mas geram ruído):
  - Scripts duplicados de jQuery e `notifications.js`: `gerar.html:304, 310, 313` e `gerar.html:308, 317`.
  - `components/topbar.html` está como documento completo em vez de puro fragmento: `src/main/resources/templates/components/topbar.html:1-9`.

## Alterações Planejadas
1. Corrigir `th:classappend` (status do colaborador)
- Substituir por expressão única com ternário aninhado:
  - `th:classappend="${c.status.name() == 'INCLUIDO' ? ' status-ativo' : (c.status.name() == 'FERIAS_PARCIAIS' ? ' status-ferias' : ' status-afastado')}"`
- Local: `src/main/resources/templates/rh/folha-pagamento/gerar.html:141-145`.

2. Remover duplicatas de scripts na página
- Manter um único `<script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>`.
- Manter um único `<script th:src="@{/js/notifications.js}"></script>`.
- Preservar `script.js` e `sidebar.js` existentes.
- Locais: `src/main/resources/templates/rh/folha-pagamento/gerar.html:304, 310, 313, 308, 317`.

3. Tornar `topbar.html` um fragmento puro (higienização)
- Remover `<!DOCTYPE html>`, `<html>`, `<head>`, `<body>` e deixar apenas `<header ... th:fragment="topbar">`.
- Ajustar a derivação do email para não depender de `[[...]]` sem `th:inline`:
  - Trocar `data-user-email="[[${...}]]"` por `th:attr="data-user-email=${#httpServletRequest.remoteUser != null ? #httpServletRequest.remoteUser : ''}"`.
- Locais: `src/main/resources/templates/components/topbar.html:1-9, 35`.

4. Pequenas melhorias (opcional)
- Trocar botões com `th:onclick` por links semânticos com `th:href` dentro de `<a>`:
  - Ex.: `th:href="@{/rh/folha-pagamento/detalhes/{id}(id=${c.id})}"`.
- Locais: `src/main/resources/templates/rh/folha-pagamento/gerar.html:152-159`.

## Validação
- Acessar `GET /rh/folha-pagamento/gerar` e confirmar renderização sem erro de parsing.
- Checar que o badge de status muda conforme `c.status` (INCLUIDO, FERIAS_PARCIAIS, demais).
- Submeter o formulário (ação `POST /rh/folha-pagamento/processar`) e validar redirecionamento.
- Monitorar o console do navegador para garantir ausência de erros por scripts duplicados.

## Observações
- O controlador já fornece os atributos esperados: `FolhaPagamentoController#gerar` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:72-86`.
- As inclusões de fragmentos (`sidebar`, `topbar`, `footer`) estão corretas; a higienização do `topbar` é para evitar marcação duplicada, não por parsing.