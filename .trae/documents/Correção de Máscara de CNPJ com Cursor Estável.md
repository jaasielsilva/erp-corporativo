## Objetivo
Corrigir a digitação do campo CNPJ para que a máscara não "bagunce" o texto enquanto o usuário digita, mantendo o cursor (caret) na posição correta.

## Estratégia
- Substituir a lógica atual de máscara por uma formatação determinística: `dd.ddd.ddd/dddd-dd`.
- Calcular o índice bruto de dígitos antes da atualização (quantos dígitos existem até a posição atual do cursor).
- Após reformatar, reposicionar o cursor no índice equivalente no texto mascarado calculando o deslocamento por separadores.
- Limitar a entrada a 14 dígitos e tratar colagem/remoção.

## Alterações
- Atualizar `static/js/cnpj-consulta.js`:
  - `formatCNPJ(s)`: constrói a máscara com separadores nos índices 1, 4, 7 e 11.
  - `rawIndexFromCaret(masked, caret)` e `caretFromRawIndex(masked, rawIdx)` para mapear corretamente a posição do cursor.
  - Listener `input`: sanitiza, limita a 14 dígitos, formata e ajusta a posição do cursor com base no mapeamento.
  - Mantém validação `isValidCnpj` anterior.

## Resultado Esperado
- Ao digitar “123456789…” o campo se manterá estável: `12.345.678/9...` sem trocas indevidas de dígitos.
- Backspace e paste funcionam naturalmente; o cursor não salta para posições erradas.

## Verificação
- Testar digitando lentamente e rapidamente 14 dígitos;
- Testar backspace sobre separadores;
- Testar colagem de CNPJ completo e parcial.
