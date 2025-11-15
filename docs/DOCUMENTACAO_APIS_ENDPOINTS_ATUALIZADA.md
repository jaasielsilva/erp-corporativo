# Documentação de APIs e Endpoints – Módulo Jurídico

## StatusContrato e Transições
- `RASCUNHO` → enviar para análise quando dados mínimos presentes.
- `EM_ANALISE` → aprovar por perfil permitido.
- `APROVADO` → assinar com data válida.
- `ASSINADO` → ativar com vigência atual e sem bloqueios.
- `ATIVO` → suspender, rescindir, renovar.
- `SUSPENSO` → reativar, rescindir.
- `VENCIDO` → renovar, rescindir.
- `RESCINDIDO`/`FINALIZADO`/`CANCELADO` → terminais.

Pré-condições básicas:
- Aprovar: contrato em `EM_ANALISE`.
- Assinar: contrato `APROVADO`, exigir `dataAssinatura`.
- Ativar: contrato `ASSINADO`, exigir vigência ok.
- Suspender: contrato `ATIVO`, exigir `motivo`.
- Reativar: contrato `SUSPENSO`.
- Rescindir: `ATIVO` ou `SUSPENSO`, exigir `motivo` e `dataRescisao`.
- Renovar: `ATIVO` ou `VENCIDO`, exigir `novasDuracaoMeses` e opcional `novoValor`.

## Filtros e Paginação – GET `/juridico/contratos`
- Parâmetros: `status`, `tipo`, `numero`, `page` (0), `size` (10).
- Model: `listaContratos`, `page`, `size`, `totalPages`, `totalElements`, `statusContrato`, `tiposContrato`.
- Métricas: `valorTotalAtivos`, `receitaMensal`, `estatisticasStatus`.

## Dashboard – GET `/juridico/api/dashboard/estatisticas`
- Campos: `contratosAtivos`, `proximosVencimentos[{titulo, contraparte, dataVencimento, valor, status}]` e agregados.

## Endpoints de Ações (PUT)
Base: `/juridico/contratos/{id}`

- `enviar-analise` (sem body)
- `aprovar` body: `{ "observacoes": "..." }`
- `assinar` body: `{ "dataAssinatura": "YYYY-MM-DD" }`
- `ativar` (sem body)
- `suspender` body: `{ "motivo": "..." }`
- `reativar` (sem body)
- `rescindir` body: `{ "motivo": "...", "dataRescisao": "YYYY-MM-DD" }`
- `renovar` body: `{ "novasDuracaoMeses": 12, "novoValor": "1500,00" }`

Exemplos:
```bash
curl -X PUT http://localhost:8080/juridico/contratos/123/enviar-analise
curl -X PUT http://localhost:8080/juridico/contratos/123/aprovar -H "Content-Type: application/json" -d '{"observacoes":"Aprovado"}'
curl -X PUT http://localhost:8080/juridico/contratos/123/assinar -H "Content-Type: application/json" -d '{"dataAssinatura":"2025-11-14"}'
curl -X PUT http://localhost:8080/juridico/contratos/123/ativar
curl -X PUT http://localhost:8080/juridico/contratos/123/suspender -H "Content-Type: application/json" -d '{"motivo":"Ajuste"}'
curl -X PUT http://localhost:8080/juridico/contratos/123/reativar
curl -X PUT http://localhost:8080/juridico/contratos/123/rescindir -H "Content-Type: application/json" -d '{"motivo":"Descumprimento","dataRescisao":"2025-12-31"}'
curl -X PUT http://localhost:8080/juridico/contratos/123/renovar -H "Content-Type: application/json" -d '{"novasDuracaoMeses":12,"novoValor":"1800,00"}'
```

## Padrões de UI
- Paleta corporativa via `corporate.css`.
- Máscaras: moeda BRL e data `DD/MM/AAAA` (convertida para `YYYY-MM-DD`).
- Paginação Bootstrap visual.
- Botões de ação condicionados ao `StatusContrato` com ícones Font Awesome.