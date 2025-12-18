## Objetivo
Exibir corretamente o Total Líquido da folha atual na página de índice `/rh/folha-pagamento`, usando os somatórios reais dos holerites.

## O que farei
- No controller `index(Model)`, quando existir `folhaAtual`, calcular o resumo via `HoleriteService.calcularResumoFolha(folhaAtual.id)` e adicionar `resumoAtual` ao modelo.
- Atualizar `templates/rh/folha-pagamento/index.html` para renderizar o Total Líquido a partir de `resumoAtual.totalLiquido` (em vez de `folhaAtual.totalLiquido`).
- Exibir um aviso discreto quando `resumoAtual.quantidadeHolerites == 0`, indicando que nenhum holerite foi gerado para a folha.

## Verificação
- Rebuild do app.
- Validar que o card “Folha do Mês Atual” mostra o Total Líquido com base nos holerites.
- Se `quantidadeHolerites` continuar 0, vamos orientar gerar/diagnosticar a folha. Caso já existam holerites, o valor será refletido corretamente.

## Observação
- A tabela de "Folhas Recentes" seguirá mostrando valores do objeto `FolhaPagamento`. Podemos alinhá-la depois para também usar os somatórios dos holerites, se necessário.