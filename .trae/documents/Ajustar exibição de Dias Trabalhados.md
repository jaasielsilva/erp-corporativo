## Contexto
- A coluna mostra `diasTrabalhados/diasMes` (template `templates/rh/folha-pagamento/gerar.html`:131).
- `diasTrabalhados` é a contagem de dias úteis (seg–sex): `ResumoFolhaService.criarResumo` (`src/main/java/com/jaasielsilva/portalceo/service/ResumoFolhaService.java`:15–24) e `isDiaUtil` (`62–65`).
- `diasMes` é o total de dias do mês (`YearMonth.lengthOfMonth()`), logo em meses de 30 dias aparece `10/30`.

## Objetivo
- Tornar a fração consistente: exibir `diasTrabalhados/diasUteisMes` (ambos só dias úteis) em vez de `diasTrabalhados/diasMes`.

## Alterações
1. DTO
- Adicionar campo `diasUteisMes` em `ColaboradorResumoFolhaDTO` (`src/main/java/com/jaasielsilva/portalceo/dto/ColaboradorResumoFolhaDTO.java`).

2. Serviço
- Em `ResumoFolhaService.criarResumo(...)`, calcular `diasUteisMes = contarDiasUteisNoMes(ym)` e preencher no DTO (`src/main/java/com/jaasielsilva/portalceo/service/ResumoFolhaService.java`).

3. Controller
- `FolhaPagamentoController.gerar` já passa o `YearMonth`; nenhuma mudança funcional necessária (`src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java`:91–99).

4. Template
- Trocar a expressão da coluna para `c.diasTrabalhados + '/' + c.diasUteisMes` em `templates/rh/folha-pagamento/gerar.html`:131.

## Validação
- Abrir `/rh/folha-pagamento/gerar` e conferir que um mês com 30 dias exibe, por exemplo, `10/22` (considerando fins de semana fora), e meses com 31 dias exibirão `10/23` conforme a distribuição de dias úteis.

## Observação
- Caso prefira manter `dias corridos` como denominador, podemos apenas documentar o comportamento atual e não alterar o código.