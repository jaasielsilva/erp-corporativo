# Documento Empresarial – Módulo Jurídico (Atualizado)

## Objetivo
Consolidar a gestão jurídica corporativa com dados reais, indicadores executivos e fluxos padronizados para contratos, processos, compliance e documentos, garantindo conformidade e rastreabilidade.

## Escopo
- Dashboard: visão geral com métricas de contratos, processos, prazos e compliance.
- Contratos: ciclo de vida completo, estatísticas e alertas automatizados.
- Processos: cadastro, audiências, prazos, andamentos e status.
- Compliance: normas, não conformidades e auditorias.
- Documentos: biblioteca com upload, categorização e histórico.

## Páginas
- `GET /juridico` → `juridico/index`
- `GET /juridico/contratos` → `juridico/contratos`
- `GET /juridico/processos` → `juridico/processos`
- `GET /juridico/compliance` → `juridico/compliance`
- `GET /juridico/documentos` → `juridico/documentos`

## APIs
- Dashboard:
  - `GET /juridico/api/dashboard/estatisticas`
- Contratos:
  - `GET /juridico/api/contratos`
  - `GET /juridico/api/contratos/{id}`
  - `POST /juridico/api/contratos`
  - `PUT /juridico/api/contratos/{id}`
  - `DELETE /juridico/api/contratos/{id}`
  - `GET /juridico/api/contratos/estatisticas`
  - Ações: `PUT /juridico/contratos/{id}/enviar-analise|aprovar|assinar|ativar|suspender|reativar|rescindir|renovar`
- Processos:
  - `GET /juridico/api/processos`
  - `GET /juridico/api/processos/{id}`
  - `POST /juridico/api/processos`
  - `GET /juridico/api/processos/{id}/audiencias`
  - `POST /juridico/api/processos/{id}/audiencias`
  - `GET /juridico/api/processos/{id}/prazos`
  - `POST /juridico/api/processos/{id}/prazos`
  - `PUT /juridico/api/prazos/{id}/concluir`
  - `PUT /juridico/api/processos/{id}/status?status=EM_ANDAMENTO|SUSPENSO|ENCERRADO`
- Compliance:
  - `GET /juridico/api/compliance/status`
  - `GET /juridico/api/compliance/normas`
  - `GET /juridico/api/compliance/nao-conformidades`
  - `GET /juridico/api/compliance/auditorias`
  - `POST /juridico/api/compliance/auditorias`
- Documentos:
  - `GET /juridico/api/documentos`
  - `POST /juridico/api/documentos/upload`
  - `POST /juridico/api/documentos/upload-multipart`
  - `GET /juridico/api/documentos/{id}`
  - `DELETE /juridico/api/documentos/{id}`

## Modelagem
- ContratoLegal: número, título, tipo, status, datas, valores, aditivos e alertas.
- ProcessoJuridico: número, tipo, tribunal, parte, assunto, status, data abertura.
- Audiencia: processoId, dataHora, tipo, observações.
- PrazoJuridico: processoId, dataLimite, descrição, responsabilidade, cumprido.
- AndamentoProcesso: processoId, dataHora, descrição, usuário.
- Norma, NaoConformidade, AuditoriaCompliance: registros de compliance.
- DocumentoJuridico: título, categoria, descrição, caminhoArquivo, criadoEm.

## Serviços
- `ProcessoJuridicoService`: métricas, urgências, audiências e prazos, conclusão de prazos, atualização de status.
- `ContratoLegalService`: ciclo de vida, estatísticas, alertas e relatórios.

## Regras
- Contratos: transições válidas de status, número único e alertas automáticos.
- Processos: prazos não retroativos, audiências vinculadas ao processo, status permitido.
- Compliance: severidade e status em não conformidades, auditorias com escopo.
- Documentos: persistência de metadados e sanitização de arquivo.

## Segurança
- Perfis com acesso ao módulo, controle de ações por perfil e auditoria de eventos.
- Proteção de dados sensíveis e logs de ações críticas.

## UX
- Dashboard com atualização periódica e componentes reutilizáveis.
- Tabelas com paginação, filtros e ordenação, badgets de status e indicadores.
