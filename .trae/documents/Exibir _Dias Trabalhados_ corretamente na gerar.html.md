## Diagnóstico
- A coluna usa `th:text="${c.diasTrabalhados + '/' + c.diasMes}"` em `templates/rh/folha-pagamento/gerar.html:137`.
- A entidade `Colaborador` não possui esses campos (`src/main/java/com/jaasielsilva/portalceo/model/Colaborador.java:26-181`).
- A rota `GET /rh/folha-pagamento/gerar` apenas injeta `colaboradores` no modelo (`src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:73-86`), sem calcular dias do mês ou dias trabalhados.
- Resultado: a expressão referencia propriedades inexistentes, e a coluna fica vazia.

## Alterações Planejadas (Fase 1 – exibir valores)
1. Adicionar `diasMesAtual` no modelo
- Em `FolhaPagamentoController#gerar` (73-86), calcular `YearMonth.of(anoAtual, mesAtual).lengthOfMonth()` e adicionar `model.addAttribute("diasMesAtual", ...)`.

2. Ajustar a expressão do template
- Em `templates/rh/folha-pagamento/gerar.html:137`, trocar por uma única expressão com fallback:
- `th:text="${(c.diasTrabalhados != null ? c.diasTrabalhados : 0) + '/' + diasMesAtual}"`.
- Assim a coluna sempre exibe `0/<dias do mês>` enquanto não houver cálculo real.

## Alterações Planejadas (Fase 2 – calcular dias trabalhados reais)
3. Introduzir cálculo real de dias trabalhados
- Criar um método em serviço (ex.: `PontoService` ou reutilizar lógica existente) que receba `colaborador`, `mes`, `ano` e retorne os dias trabalhados.
- Na `FolhaPagamentoController#gerar`, iterar sobre `colaboradores` e anexar os valores (via DTO `ColaboradorResumoFolhaDTO { id, nome, departamento, salarioBase, diasTrabalhados }`).
- Renderizar a lista de DTOs na view em vez de `Colaborador`, atualizando os bindings (`c.diasTrabalhados`).

## Validação
- Abrir `GET /rh/folha-pagamento/gerar` e confirmar a renderização da coluna como `0/<dias do mês>` (Fase 1).
- Após implementar o serviço (Fase 2), verificar que os valores reais são exibidos conforme registros de ponto.

## Observações
- Manter o cálculo de `diasMesAtual` sincronizado com os valores selecionados no formulário se a página aceitar mudar mês/ano antes do processamento.
- Se houver folha existente para o mês corrente, pode-se opcionalmente usar `Holerite.diasTrabalhados` já calculado para preencher os números antes do processamento.