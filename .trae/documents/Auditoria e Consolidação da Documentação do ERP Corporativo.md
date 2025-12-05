## Diagnóstico Inicial
- Versão backend: `pom.xml` indica Spring Boot `3.5.5` e artefato `portal-ceo-0.0.1-SNAPSHOT`.
- JAR nos documentos: aparecem `erp-corporativo-1.0.0.jar` e `painel-do-ceo-0.0.1-SNAPSHOT.jar` (inconsistentes com `artifactId=portal-ceo`).
- Front-end em templates: Bootstrap `5.3.0`/`5.3.2`/`5.3.3`, Font Awesome `6.0.0` e `6.4.0`, jQuery `3.7.0`.
  - Exemplos: `src/main/resources/templates/chat/index.html:7` (Bootstrap 5.3.0), `src/main/resources/templates/marketing/dashboard/index.html:14` (FA 6.0.0), `src/main/resources/templates/juridico/documentos.html:264` (Bootstrap 5.3.3).
- OpenAPI: não há arquivos `.yml/.yaml/.json` oficiais; existem docs textuais de endpoints.
- Duplicações/overlaps principais:
  - Sistema completo: `docs/README.md`, `DOCUMENTACAO_SISTEMA_COMPLETA.md`, `DOCUMENTACAO_COMPLETA_SISTEMA.md`, `DOCUMENTACAO_COMPLETA_SISTEMA_UNIFICADA.md`.
  - RH: múltiplos “Completa/Atualizada/Índice/Manual”.
  - Jurídico: dois “APIs e Endpoints” + múltiplos “Completa/Atualizada/Manual”.
  - Suporte/Chamados: vários guias/fluxos/fluxogramas/tech docs.
  - Chat Interno: “Manual”, “Completa” e “Análise/Proposta”.

## Critérios de Compatibilidade
- Backend: padronizar todos os documentos para Spring Boot `3.5.5` (fonte: `pom.xml`).
- Artefato: referenciar `portal-ceo-0.0.1-SNAPSHOT.jar` como nome oficial; registrar divergências anteriores em “Histórico”.
- Front-end: adotar Bootstrap `5.3.3`, Font Awesome `6.4.0`, jQuery `3.7.0` como padrão documental; indicar páginas que ainda usam versões inferiores e listar para correção futura.
- APIs: gerar índice de endpoints a partir dos controllers Java e sincronizar com docs de API.

## Consolidação por Grupo
- Sistema completo:
  - Canonizar em `DOCUMENTACAO_COMPLETA_SISTEMA_UNIFICADA.md`.
  - Fundir conteúdo de `docs/README.md`, `DOCUMENTACAO_SISTEMA_COMPLETA.md` e `DOCUMENTACAO_COMPLETA_SISTEMA.md` removendo redundâncias.
  - Deixar `README.md` raiz como sumário e ponte para o documento canônico.
- RH:
  - Canonizar em `docs/DOCUMENTACAO_MODULO_RH_COMPLETA_ATUALIZADA.md`.
  - Incorporar `DOCUMENTACAO_MODULO_RH.md` e `DOCUMENTACAO_MODULO_RH_INDICE.md`. Manter `manual-usuario-rh-ponto.md` como manual do usuário.
  - Corrigir comandos de build/exec para o artefato oficial.
- Jurídico:
  - Canonizar em `DOCUMENTACAO_MODULO_JURIDICO_COMPLETA.md`.
  - Unificar `DOCUMENTACAO_APIS_ENDPOINTS_ATUALIZADA.md` + `DOCUMENTACAO_APIS_ENDPOINTS.md` em um único “APIs e Endpoints – Jurídico”, gerado dos controllers.
  - Manter `MANUAL_USUARIO_JURIDICO.md` como manual.
- Suporte/Chamados:
  - Canonizar em `documentacao-tecnica-suporte.md`.
  - Integrar `guia-implementacao-suporte.md`, `fluxo-modulo-suporte-atualizado.md`, `fluxograma-modulo-suporte.md` e `DOCUMENTACAO_FLUXO_CHAMADOS.md`. Preservar `guia-rapido-chamados.md` como referência rápida.
- Chat Interno:
  - Canonizar em `DOCUMENTACAO_CHAT_INTERNO_COMPLETA.md`. Atualizar versões de libs e alinhar com artefato oficial.
  - Arquivar `ANALISE_CHAT_INTERNO_PROPOSTA.md` como histórico técnico.

## Padronização de Formato e Terminologia
- Template único para todos os documentos (Front Matter + seções):
  - Título, Módulo, Compatibilidade (Spring Boot `3.5.5`), Artefato (`portal-ceo-0.0.1-SNAPSHOT.jar`), Front-end adotado (Bootstrap `5.3.3`, FA `6.4.0`, jQuery `3.7.0`), Última revisão, Status.
  - Sumário, Visão Geral, Arquitetura, Endpoints, Fluxos, Instalação/Execução, Dependências, Segurança/Compliance, Versionamento, Histórico de alterações.
- Glossário/terminologia: unificar termos (“Chamados”, “Workflow”, “Compliance”, “Indicadores”, “Vale Transporte”, “Ponto/Escalas”).
- Nomenclatura de arquivos: `DOCUMENTACAO_MODULO_<NOME>.md` para técnica; `MANUAL_USUARIO_<NOME>.md` para uso; `APIS_ENDPOINTS_<NOME>.md` para APIs.

## Procedimento de Atualização
- Auditoria automática de endpoints: varrer `src/main/java` por `@GetMapping/@PostMapping/...` e gerar tabela de rotas por módulo.
- Versões e artefato: corrigir cabeçalhos, comandos e seções “Tecnologias” para refletir as versões alvo.
- Deduplicação: mover trechos repetidos para “Ver também/Referências” ou incorporar no documento canônico.
- Consistência visual: alinhar exemplos de CDN e snippets com o padrão adotado.
- Validação cruzada: para cada documento, referenciar trechos de código relevantes (`controller`, `template`) com `file_path:line_number` quando aplicável.

## Relatório e Backup
- Relatório `docs/relatorio-atualizacao-documentacao.md` contendo:
  - Lista de arquivos alterados/mesclados, justificativas e impactos.
  - Tabelas de versões antes/depois (Spring Boot, Bootstrap, FA, jQuery).
  - Mapa de documentos canônicos e os que foram arquivados.
- Backup: mover originais para `docs/_backup/<YYYY-MM-DD>/`.

## Entregáveis
- Conjunto unificado e atualizado de `.md` por módulo e documento canônico do sistema.
- Índice `docs/INDEX.md` com links, escopos e compatibilidade.
- Relatório de alterações e pasta de backup preservando histórico.

## Riscos e Validações
- Versões de front-end mistas no código: a documentação adotará padrão e marcará divergências por página até que o código seja corrigido.
- Ausência de OpenAPI: opcionalmente incluir plano de adoção (Springdoc) após consolidação documental.
- Verificação final: checagem de referências (ex.: `chat/index.html:7`, `marketing/dashboard/index.html:14`, `juridico/documentos.html:264`) e links internos funcionando.
