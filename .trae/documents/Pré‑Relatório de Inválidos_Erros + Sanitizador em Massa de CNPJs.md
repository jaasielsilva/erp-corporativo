## Objetivo
Adicionar pré‑relatório com amostras de CNPJs inválidos/erro após processamento e criar um sanitizador em massa para normalizar `cpfCnpj` (remover máscara) e detectar inválidos, com opção de aplicar.

## Alterações
- Serviço de processamento: coletar amostras (até 20) de inválidos/erros e expor no DTO de status.
- UI: painel “Pré‑relatório” com listas para inválidos e erros.
- Sanitizador: serviço transacional que percorre `Cliente` PJ, normaliza `cpfCnpj` e classifica inválidos; endpoint `POST /utilidades/sanitizar-cnpj?apply=true|false`.
- UI: botão “Sanitizar CNPJs” na página de Utilidades com toast de resultado.

## Implementação
- DTO: adicionar arrays `invalidSamples`, `errorSamples`.
- ProcessamentoCnpjService: listas thread‑safe, preencher amostras e contadores, retornar no status.
- Controller: adicionar endpoint do sanitizador.
- Repository: método para buscar PJs com `cpfCnpj` não nulo.
- JS/HTML: renderizar relatório e adicionar botão para sanitização.

## Verificação
- Com base populada: executar processamento e ver contagem + amostras.
- Rodar sanitização em modo dry‑run; depois aplicar.
