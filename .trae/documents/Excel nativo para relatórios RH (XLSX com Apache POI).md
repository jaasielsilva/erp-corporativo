## Contexto atual
- O endpoint `exportarRelatorioExcel` envia CSV com `Content-Type` de Excel (`RhController.java:299–317`), o que não preserva tipos, formatos, larguras e estilos.
- O frontend também gera CSV no navegador (`templates/rh/folha-pagamento/relatorios.html`).

## Objetivo
- Gerar planilhas Excel (.xlsx) nativas com colunas/linhas e formatação correta (tipos numéricos, datas, moeda, largura de coluna, cabeçalhos, auto-filtro, congelamento de painéis).

## Dependência
- Adicionar `org.apache.poi:poi-ooxml` (versão estável 5.2.x) ao `pom.xml` para criação de arquivos XLSX.
- Opcional: usar `SXSSFWorkbook` para streaming quando houver grande volume.

## Backend
- Criar novo endpoint `GET /rh/api/relatorios/exportar/xlsx` no `RhController` que:
  - Carrega os dados via `RhRelatorioService` conforme `tipo` (folha-analitica, gerencial, obrigacoes, beneficios).
  - Monta `XSSFWorkbook` e uma `Sheet` por relatório (ou uma única `Sheet` conforme o tipo escolhido).
  - Aplica estilos: cabeçalho em negrito, preenchimento, `freezePane(1,0)`, `autoFilter`, `autoSizeColumn`.
  - Define tipos corretos: números como `double`, datas com `DataFormat` e moeda `#,##0.00`.
  - Retorna `ResponseEntity<byte[]>` com `Content-Type` `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` e arquivo `.xlsx`.
- Implementar helpers por tipo:
  - `montarFolhaAnaliticaSheet(...)`
  - `montarGerencialSheet(...)`
  - `montarObrigacoesSheet(...)`
  - `montarBeneficiosSheet(...)`

## Frontend
- Atualizar `relatorios.html` para oferecer opção "Excel (XLSX)" e chamar o novo endpoint `/exportar/xlsx`.
- Manter opção CSV existente para quem preferir.

## Formatação e usabilidade
- Cabeçalho com estilo e filtro automático.
- Largura de colunas ajustada (`autoSizeColumn`).
- Congelar a primeira linha para facilitar navegação.
- Moeda com separador de milhar e duas casas (`#,##0.00`).
- Datas em formato legível (`dd/MM/yyyy`).

## Compatibilidade
- Manter `/exportar/excel` (CSV) temporariamente para não quebrar fluxos existentes; marcar como legado na UI.

## Testes e validação
- Baixar arquivos de cada tipo e verificar em Excel: formatos, filtros, congelamento, largura das colunas.
- Verificar tamanho e desempenho com dados maiores; trocar para `SXSSFWorkbook` se necessário.

## Segurança e acesso
- Preservar as mesmas regras de autorização (`@PreAuthorize`) dos endpoints atuais.

## Alternativa (template)
- Se desejado, usar JXLS para preencher um template `.xlsx` com os beans dos relatórios; facilita manutenção visual, mas adiciona outra dependência.