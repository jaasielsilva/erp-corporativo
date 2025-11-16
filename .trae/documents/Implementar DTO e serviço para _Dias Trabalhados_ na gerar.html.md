## Objetivo
- Exibir "Dias Trabalhados" com valores calculados por colaborador para o mês/ano de referência na página `gerar.html` usando um DTO e um serviço dedicado.

## Implementação
1. Criar DTO `ColaboradorResumoFolhaDTO`
- Campos: `id`, `nome`, `cargoNome`, `departamentoNome`, `salario`, `diasTrabalhados`, `diasMes`, `status`.

2. Criar serviço `ResumoFolhaService`
- Método `criarResumo(Colaborador c, YearMonth ym)` calcula dias úteis:
  - Se o colaborador estiver inativo, `diasTrabalhados = 0`.
  - Se `ym` for o mês atual, contar dias úteis até hoje.
  - Caso contrário, contar dias úteis do mês inteiro.
- Define `diasMes` como número total de dias do mês (`ym.lengthOfMonth()`).

3. Atualizar o controller `FolhaPagamentoController#gerar`
- Injetar `ResumoFolhaService`.
- Construir lista de `ColaboradorResumoFolhaDTO` com base em `listarAtivos()` e `YearMonth` do mês atual.
- Adicionar `colaboradoresResumo` ao modelo.

4. Atualizar o template `gerar.html`
- Iterar sobre `colaboradoresResumo` em vez de `colaboradores`.
- Ajustar bindings: nome, cargo, departamento, salário, dias trabalhados (`c.diasTrabalhados + '/' + c.diasMes`).
- Manter o status usando `c.status` do DTO na classe condicional (`th:classappend`).
- Trocar `<select>` de departamentos para opções dinâmicas vindas de `departamentos`.

## Validação
- Renderizar `GET /rh/folha-pagamento/gerar` e confirmar:
  - Coluna “Dias Trabalhados” mostra valores calculados.
  - Sem erros de parsing.
  - Select de departamentos usa dados do modelo.

## Observações
- A lógica de dias trabalhados é previsional (dias úteis). Integração com registros de ponto pode ser adicionada depois para valores reais.