# Manual do Usuário — Jurídico > Previdenciário (INSS)

## Metadados
- Título: Manual do Usuário — Módulo Previdenciário (INSS)
- Módulo: Jurídico > Previdenciário (INSS)
- Última revisão: 2025-12-24
- Status: Em uso

## Visão Geral
O módulo **Previdenciário (INSS)** serve para:
- Criar um processo previdenciário vinculado a um **Cliente** e um **Responsável**
- Acompanhar o processo por **etapas (workflow)**
- Anexar **documentos/laudos** em etapas que permitem anexo
- Registrar tudo em um **Histórico** (mudança de etapa e uploads)

## O processo tem ID único?
Sim. Cada processo é salvo no banco com um identificador único `id` (chave primária) gerado automaticamente.
- O `id` é gerado via `@GeneratedValue(strategy = GenerationType.IDENTITY)` em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/processo/entity/ProcessoPrevidenciario.java:31-33`.
- Na tela, esse mesmo `id` é exibido como “Processo #<id>” em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/processo/controller/ProcessoPrevidenciarioController.java:121-122`.

Observação: no upload de documentos, o arquivo salvo também recebe um prefixo UUID para evitar colisão de nomes (isso é do arquivo, não do processo) em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/documentos/service/DocumentoProcessoService.java:67-70`.

## Status e Etapas
### Status do processo
O processo possui 3 status:
- `ABERTO`
- `EM_ANDAMENTO`
- `ENCERRADO`

Eles são definidos em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/processo/entity/ProcessoPrevidenciarioStatus.java:3-7`.

Regras práticas:
- Ao avançar para qualquer etapa diferente de `FINALIZADO`, se o processo ainda está `ABERTO`, ele muda para `EM_ANDAMENTO`.
- Ao ir para `FINALIZADO`, o processo muda para `ENCERRADO` e grava a data de encerramento.

Implementação em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/workflow/service/WorkflowService.java:77-83`.

### Etapas (workflow)
As etapas do módulo são:
- `CADASTRO`
- `DOCUMENTACAO`
- `ANALISE`
- `PROTOCOLO_INSS`
- `ACOMPANHAMENTO`
- `DECISAO`
- `RECURSO`
- `FINALIZADO`

Elas estão em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/workflow/entity/EtapaWorkflowCodigo.java:3-12`.

## Documentos (anexos)
### Tipos de documentos disponíveis
Os tipos aceitos para anexo (selecionáveis na tela) são:
- `RG`
- `CPF`
- `COMPROVANTE_RESIDENCIA`
- `PROCURACAO`
- `LAUDO_MEDICO`
- `OUTROS`

Definição em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/documentos/entity/TipoDocumentoProcesso.java:3-10`.

### Onde os arquivos ficam
Por padrão, os uploads vão para:
- `uploads/juridico/previdenciario/processos/<processoId>/...`

Configuração em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/documentos/service/DocumentoProcessoService.java:36-37`.

### Quando é permitido anexar
Cada etapa tem a flag “permite anexo”. Se a etapa atual não permitir anexo, o sistema bloqueia o upload.
- Validação em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/documentos/service/DocumentoProcessoService.java:59-62`.

## Caso de uso real (passo a passo)
### Cenário
Você é do Jurídico e vai abrir um processo de benefício no INSS para um cliente.

Dados do exemplo:
- Cliente: `João da Silva (CPF 123.456.789-00)` (cadastro já existente em **Comercial > Clientes**)
- Responsável: `Master do Sistema`
- Objetivo: organizar os documentos, controlar o andamento por etapa e guardar histórico do processo.

### 1) Criar o processo (Etapa `CADASTRO`)
1. Acesse: **Jurídico > Previdenciário (INSS)**.
2. Clique em **Novo processo**.
3. Preencha:
   - `Cliente`: selecione `João da Silva`
   - `Responsável`: selecione `Master do Sistema`
4. Clique em **Salvar**.

Resultado esperado:
- O sistema cria o processo e atribui um `id` único.
- O histórico registra a abertura.
- A tela de detalhes passa a exibir “Processo #<id>”, “Status”, “Etapa atual” e “Abertura”.

Criação via endpoint `POST /juridico/previdenciario/salvar` em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/processo/controller/ProcessoPrevidenciarioController.java:73-87`.

### 2) Ir para `DOCUMENTACAO` e anexar documentos
Na tela do processo (detalhe), use **Mudar etapa** e selecione `DOCUMENTACAO`.

Depois, no card **Documentos**, anexe:
- Tipo `RG` + arquivo `rg_joao.pdf`
- Tipo `CPF` + arquivo `cpf_joao.pdf`
- Tipo `COMPROVANTE_RESIDENCIA` + arquivo `conta_luz.pdf`
- Tipo `PROCURACAO` + arquivo `procuracao_assinada.pdf`

Resultado esperado:
- Cada upload aparece na tabela “Documentos”.
- Cada upload gera um evento no **Histórico** (“UPLOAD_DOCUMENTO … na etapa DOCUMENTACAO”).

Upload via endpoint `POST /juridico/previdenciario/{id}/anexar-documento` em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/documentos/controller/DocumentoProcessoController.java:27-41`.

### 3) Ir para `ANALISE`
Use **Mudar etapa** e selecione `ANALISE`.

O que fazer nessa etapa (uso prático):
- Conferir se todos os documentos necessários foram anexados.
- Se faltar algo, volte para `DOCUMENTACAO` usando o seletor de etapa e anexe os documentos faltantes.

### 4) Ir para `PROTOCOLO_INSS`
Use **Mudar etapa** e selecione `PROTOCOLO_INSS`.

O que fazer nessa etapa (uso prático):
- Registrar no processo que a documentação está pronta para protocolo (o módulo registra a mudança no histórico).
- Se o seu fluxo exigir anexar comprovantes adicionais, volte para a etapa anterior (se permitido) e anexe.

### 5) Ir para `ACOMPANHAMENTO` e anexar laudos/andamentos
Use **Mudar etapa** e selecione `ACOMPANHAMENTO`.

Nessa etapa, um uso real é anexar material que chega durante o acompanhamento:
- Tipo `LAUDO_MEDICO` + arquivo `laudo_atualizado.pdf`
- Tipo `OUTROS` + arquivo `comprovante_agendamento.pdf`

### 6) Ir para `DECISAO`
Use **Mudar etapa** e selecione `DECISAO`.

O que fazer nessa etapa (uso prático):
- Consolidar os anexos e preparar o registro interno.
- Se ainda precisar anexar algo e a etapa não permitir, volte para uma etapa anterior que permita anexo e faça o upload.

### 7) Ir para `RECURSO` (se aplicável)
Use **Mudar etapa** e selecione `RECURSO`.

O que fazer nessa etapa (uso prático):
- Controlar que o processo está na fase recursal.
- Anexar documentos complementares se a etapa permitir.

### 8) Finalizar (`FINALIZADO`)
Use **Mudar etapa** e selecione `FINALIZADO`.

Resultado esperado:
- Status vira `ENCERRADO`.
- Data de encerramento é preenchida.
- O histórico registra “De RECURSO para FINALIZADO” (ou da etapa que você veio).

## Como o seletor “Mudar etapa” decide as opções
As opções do seletor são calculadas por transições permitidas do workflow:
- A tela monta o seletor com `destinosPermitidos` em `src/main/resources/templates/juridico/previdenciario/detalhe.html:36-47`.
- O backend calcula `destinosPermitidos` a partir das transições cadastradas e roles do usuário em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/processo/controller/ProcessoPrevidenciarioController.java:90-113` e `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/workflow/service/WorkflowService.java:46-63`.
- As transições padrão (incluindo retorno) são garantidas na inicialização em `src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/workflow/WorkflowPrevidenciarioInitializer.java:24-63`.

