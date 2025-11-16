## Objetivo
- Criar um documento único e claro com o fluxo completo da página `http://localhost:8080/rh/folha-pagamento/gerar`, incluindo um fluxograma visual e um checklist de testes para cada botão/campo/ação.

## Conteúdo do Documento
1. Visão Geral
- Propósito da página e seções principais.
- Origem dos dados no Controller (`FolhaPagamentoController.gerar`).

2. Mapa de Dados
- Variáveis de modelo: `existeFolhaAtual`, `mesAtual`, `anoAtual`, `diasMesAtual`, `colaboradoresResumo`.
- Como `ResumoFolhaService.criarResumo` calcula `diasTrabalhados/diasUteisMes/diasMes`.

3. Componentes da UI
- Configurações da Folha (form POST `/rh/folha-pagamento/processar`).
- Colaboradores Incluídos (tabela e badges).
- Resumo Financeiro (valores estáticos ilustrativos).
- Ações da Folha (fluxo em 4 etapas com funções JS).

4. Fluxograma Visual (Mermaid)
- Fluxo: carregar página → preparar modelo → renderizar seções → submeter form → processamento/redirect.
- Fluxo de ações: Calcular → Revisar → Gerar Holerites → Aprovar (com estados disabled/habilitado).

5. Checklist de Testes
- Campos obrigatórios, selects, botões do topo, alerta de folha existente.
- Botões das etapas (estados, transições e mensagens).
- Tabela: formatação, badges e ações por linha.

6. Observações
- Comportamento de simulação nos botões de etapas (JS de front-end) vs processamento real via POST.

## Implementação
- Adicionar arquivo `docs/rh/folha-pagamento/gerar-fluxo.md` com o conteúdo acima e o fluxograma em Mermaid.

## Validação
- Abrir a página e seguir o checklist, confirmando cada expectativa.
- Verificar que o fluxograma representa fielmente as transições da UI e chamadas do backend.

## Entrega
- Documento versionado no repositório (sem alterar lógica), pronto para revisão e testes.