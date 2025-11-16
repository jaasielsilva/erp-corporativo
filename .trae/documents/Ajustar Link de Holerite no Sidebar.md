## Objetivo
- Substituir o método `holeritePdf` para gerar PDF com layout empresarial (cabeçalho, identificação, Proventos/Descontos em duas colunas com cores, resumo e informações adicionais), usando Lowagie/iText.

## Alterações no Controller
- Arquivo: `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java`
- Imports: adicionar `PdfPTable`, `PdfPCell`, `NumberFormat`, `Locale`, `Color`.
- Método `holeritePdf(@PathVariable Long id, ...)`:
  - Cabeçalho: empresa à esquerda; à direita "HOLERITE", "Competência" (via `holeriteService.gerarDescricaoPeriodo`), "Emitido em" (mês/ano atual).
  - Identificação (2 colunas): Funcionário/Cargo/Departamento/Admissão; E‑mail/CPF/Salário Base/Dias Trabalhados.
  - Corpo (2 colunas):
    - Proventos: salário base, horas extras, adicional noturno, comissões, VR, VT, total; header verde claro.
    - Descontos: INSS, IRRF, FGTS, plano, desconto VR/VT, total; header vermelho claro.
  - Resumo (3 caixas): Total Proventos, Total Descontos, Salário Líquido; bordas azuis.
  - Informações adicionais: bases INSS/IRRF/FGTS, dependentes (0), salário família (0), 13º proporcional = `salarioBase/12`.
  - Rodapé institucional.
  - Formatação: `NumberFormat.getCurrencyInstance(new Locale("pt","BR"))`, padding, bordas e cores.

## Links de Download
- Garantir `th:href="@{/rh/folha-pagamento/holerite/{id}/pdf(id=${...})}"` em:
  - `visualizar.html` (tabela de holerites)
  - `holerite.html` (botão “Baixar PDF”).

## Validação
- Reiniciar a app.
- Acessar `holerite/{id}/pdf` e verificar o layout: cabeçalho, colunas com cores e totais, resumo e rodapé.

## Observação
- PDF não interpreta CSS diretamente; estilização é simulada via tabelas/cores/padding no iText. Ajusto as cores para combinar com seu HTML atual.