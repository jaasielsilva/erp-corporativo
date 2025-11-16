## Diagnóstico Atual
- Persistem erros de parsing em `templates/rh/folha-pagamento/gerar.html` por sintaxe inválida em `th:classappend` com múltiplos `${...}` encadeados (`src/main/resources/templates/rh/folha-pagamento/gerar.html:137–141`).
- Há múltiplas inclusões duplicadas de jQuery e `notifications.js` que geram ruído e podem causar conflitos (`gerar.html:300–314`).
- Botões de ação usam `th:onclick` com rotas inconsistentes (`/folha/...` em vez de `/rh/folha-pagamento/...`) em `gerar.html:147–155`.
- O servidor local não inicia devido a porta `8080` ocupada, impedindo validação via navegador.

## Correções Planejadas
1. Corrigir `th:classappend` de Status
- Substituir por uma única expressão ternária aninhada:
- `th:classappend="${c.status.name() == 'ATIVO' ? ' status-ativo' : (c.status.name() == 'SUSPENSO' ? ' status-afastado' : ' status-ferias')}"`
- Local: `src/main/resources/templates/rh/folha-pagamento/gerar.html:137–141`.

2. Remover scripts duplicados
- Manter um único `<script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>`.
- Manter um único `<script th:src="@{/js/notifications.js}"></script>`.
- Preservar `script.js` e `sidebar.js`.
- Local: `gerar.html:300–314`.

3. Corrigir rotas dos botões
- Trocar `th:onclick` por links semânticos com `th:href` para rotas válidas:
- Detalhes: `th:href="@{/rh/folha-pagamento/detalhes/{id}(id=${c.id})}"`
- Editar: `th:href="@{/rh/folha-pagamento/editar/{id}(id=${c.id})}"`
- Local: `gerar.html:147–155`.

4. Garantir exibição de Dias Trabalhados
- Já usamos `diasMesAtual` (`gerar.html:135`). Confirmar variável no modelo; se o mês/ano forem ajustados pelo formulário, atualizar cálculo via JS ou re-render com os novos valores.

## Validação (Login e Página)
1. Liberar porta e subir servidor
- Encerrar processos na `8080` ou configurar `server.port=8081` temporariamente para testes.

2. Login no sistema
- Acessar `http://localhost:8080/login` (ou `:8081`).
- Usar usuário de desenvolvimento existente; caso necessário, verifico configuração de segurança e usuários seed.

3. Verificação da página
- Navegar para `http://localhost:8080/rh/folha-pagamento/gerar`.
- Confirmar:
  - Sem erro de parsing.
  - Coluna “Dias Trabalhados” exibindo `0/<dias do mês>`.
  - Status com classe correta.
  - Ações de navegação com rotas válidas.
  - Console sem erros de script duplicado.

## Observações
- Se existirem folhas já geradas, podemos puxar `Holerite.diasTrabalhados` para preencher valores reais.
- `topbar.html` já está como fragmento puro; manter consistente com `sidebar` e `footer`. 