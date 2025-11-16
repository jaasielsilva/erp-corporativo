## Diagnóstico
- O template `holerite.html` referencia `holerite.colaborador.matricula` (linha 264), porém `Colaborador` não possui o campo `matricula` — quem tem é `Usuario`. Isso causa erro de renderização ao abrir `/rh/folha-pagamento/holerite/{id}`.
- Demais campos usados já são nulosafe: `cargo`, `departamento`, `dataAdmissao` têm verificações.

## Correção
1. Atualizar `src/main/resources/templates/rh/folha-pagamento/holerite.html`:
- Remover a linha da “Matrícula” ou substituí-la por um campo existente (ex.: `holerite.colaborador.email` com fallback `'-'`).
- Manter consistência visual da seção.

## Validação
- Abrir `/rh/folha-pagamento/holerite/{id}` (ex.: o holerite associado à folha 1) e confirmar que a página carrega sem erro e os valores estão corretos.

## Observação
- Se desejar exibir matrícula real, será necessário relacionar `Usuario` ao `Colaborador` no holerite ou carregar via serviço adicional; por ora, usamos `email` para referência do colaborador.