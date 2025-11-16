# Fluxo da Página "Gerar Folha de Pagamento"

## Visão Geral
- URL: `http://localhost:8080/rh/folha-pagamento/gerar`
- Objetivo: configurar parâmetros da folha, visualizar colaboradores e executar etapas de processamento (simuladas na UI) antes de gerar/aprovar.
- Dados carregados pelo controller: `FolhaPagamentoController.gerar` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:80–101`.

## Mapa de Dados (Model)
- `existeFolhaAtual`: indica se já existe folha para o mês atual (`FolhaPagamentoController.java:86–92`).
- `mesAtual` e `anoAtual`: mês/ano corrente (`FolhaPagamentoController.java:89–91`).
- `diasMesAtual`: total de dias corridos no mês (`FolhaPagamentoController.java:91–92`).
- `colaboradoresResumo`: lista criada por `ResumoFolhaService.criarResumo` (`FolhaPagamentoController.java:93–99`).
- `ResumoFolhaService.criarResumo`: retorna `diasTrabalhados`, `diasUteisMes`, `diasMes` (`src/main/java/com/jaasielsilva/portalceo/service/ResumoFolhaService.java:14–26,32–42`).

## Componentes da UI
- Topo de ações: links “Ver Relatórios” e “Histórico” (`templates/rh/folha-pagamento/gerar.html:24–31`).
- Configurações da Folha: formulário com selects de mês/ano/dep/tipo e botão “Processar Folha” (POST `/rh/folha-pagamento/processar`) (`templates/rh/folha-pagamento/gerar.html:33–81`).
- Colaboradores Incluídos: tabela com nome, cargo, departamento, salário e `Dias Trabalhados` (mostra `diasTrabalhados/diasUteisMes`) e status (`templates/rh/folha-pagamento/gerar.html:83–158, 120–151, 131, 134–137`).
- Resumo Financeiro: valores ilustrativos de proventos/descontos/líquido (`templates/rh/folha-pagamento/gerar.html:159–193`).
- Ações da Folha: 4 etapas sequenciais com botões e estados `disabled` (`templates/rh/folha-pagamento/gerar.html:195–239`).

## Fluxo de Backend
- Submissão do formulário: `POST /rh/folha-pagamento/processar` (`FolhaPagamentoController.java:107–128`).
- Serviço chamado: `FolhaPagamentoService.gerarFolhaPagamento(mes, ano, usuario)` (`FolhaPagamentoController.java:117`).
- Redirecionamento após sucesso: `redirect:/rh/folha-pagamento/visualizar/{id}` (`FolhaPagamentoController.java:122–123`).

## Fluxo de Ações (UI)
- Etapas e funções JS em `templates/rh/folha-pagamento/gerar.html:250–291`:
  - `calcularFolha()`
    - Simula processamento por `setTimeout(3000ms)`, muda rótulo/estilo do botão para “Calculado” e habilita “Revisar Cálculos”.
  - `revisarCalculos()`
    - Após etapa atual ≥ 1, habilita “Gerar Holerites”.
  - `gerarHolerites()`
    - Após etapa atual ≥ 2, habilita “Aprovar Folha”.
  - `aprovarFolha()`
    - Após etapa atual ≥ 3, apresenta `confirm()` e exibe mensagem de aprovação.

## Fluxograma Visual

```mermaid
flowchart TD
  A[Usuário abre /rh/folha-pagamento/gerar] --> B[Controller prepara modelo
  - existeFolhaAtual
  - mesAtual/anoAtual
  - diasMesAtual
  - colaboradoresResumo]
  B --> C[Renderiza seções
  - Configurações da Folha
  - Colaboradores Incluídos
  - Resumo Financeiro
  - Ações da Folha]
  C --> D{Usuário preenche Configurações}
  D -->|POST /rh/folha-pagamento/processar| E[Gerar Folha (backend)
  - FolhaPagamentoService.gerarFolhaPagamento
  - redirect visualizar/{id}]

  C --> F[Etapas (UI)]
  F --> F1[1. Calcular Folha
  - Simula processamento
  - Habilita Revisar]
  F1 --> F2[2. Revisar Cálculos
  - Habilita Gerar Holerites]
  F2 --> F3[3. Gerar Holerites
  - Habilita Aprovar]
  F3 --> F4[4. Aprovar Folha
  - Confirm e aviso]
```

## Checklist de Testes

### Configurações da Folha
- Verificar `existeFolhaAtual`: quando verdadeiro, alerta aparece (`templates/rh/folha-pagamento/gerar.html:36–38`).
- Selecionar `mes`, `ano`: obrigatórios (`templates/rh/folha-pagamento/gerar.html:43–53`).
- Selecionar `departamento`: opcional (`templates/rh/folha-pagamento/gerar.html:58–63`).
- Selecionar `tipoFolha`: padrão “Folha Normal” (`templates/rh/folha-pagamento/gerar.html:65–72`).
- Clicar “Processar Folha”: deve enviar `POST` e redirecionar em sucesso (`FolhaPagamentoController.java:107–128`).

### Colaboradores Incluídos
- Conferir exibido `Dias Trabalhados` como `x/y` onde `y` = dias úteis do mês (`templates/rh/folha-pagamento/gerar.html:131`).
- Conferir badge de `status`: cores e rótulos compatíveis (`templates/rh/folha-pagamento/gerar.html:134–137`).
- Botões de ação por linha (ícone olho/editar) reagem ao clique (`templates/rh/folha-pagamento/gerar.html:140–149`).

### Ações da Folha (Etapas)
- Estado inicial: apenas “Calcular Folha” habilitado (`templates/rh/folha-pagamento/gerar.html:195–207`).
- “Calcular Folha”: altera para “Calculado”, habilita “Revisar Cálculos” (`templates/rh/folha-pagamento/gerar.html:250–267`).
- “Revisar Cálculos”: habilita “Gerar Holerites” (`templates/rh/folha-pagamento/gerar.html:269–275`).
- “Gerar Holerites”: habilita “Aprovar Folha” (`templates/rh/folha-pagamento/gerar.html:277–283`).
- “Aprovar Folha”: confirma e mostra mensagem (`templates/rh/folha-pagamento/gerar.html:285–291`).

### Alertas e Visual
- Alerta de aviso estilizado (`.alert.alert-warning`) aparece quando há folha atual (`style.css` e template).
- Botões têm visuais padronizados com `.btn`, variáveis `.btn-primary/secondary/success` (`src/main/resources/static/css/style.css:2002+`).
- Badges `status-afastado` e `status-ferias` estilizadas (`style.css:2002+`).

## Observações
- As etapas 1–4 são simulações de UI para facilitar o fluxo; o processamento real ocorre no submit do formulário para `/processar`.
- O redirecionamento após gerar a folha leva à página de visualização, onde holerites e totais são exibidos.