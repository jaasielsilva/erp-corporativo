# Relatório de Atualização da Documentação

## Resumo
- Consolidação de documentos por módulo e padronização de versões/artefatos.
- Remoção de redundâncias com backup em `docs/_backup/2025-12-05/`.

## Alterações Realizadas
- Sistema (canônico): `DOCUMENTACAO_COMPLETA_SISTEMA_UNIFICADA.md` mantido.
- README raiz: atualizado clone, `cd`, banco `portal_ceo` e JAR `portal-ceo-0.0.1-SNAPSHOT.jar`.
- `docs/README.md`: atualizado para Spring Boot 3.5.5 e link canônico.
- Removidos (backup criado):
  - `docs/DOCUMENTACAO_COMPLETA_SISTEMA.md`
  - `docs/DOCUMENTACAO_SISTEMA_COMPLETA.md`
  - `docs/DOCUMENTACAO_MODULO_RH.md`
  - `docs/DOCUMENTACAO_MODULO_RH_INDICE.md`
  - `docs/ANALISE_MODULO_RH_COMPLETA.md`
  - `docs/DOCUMENTACAO_MODULO_JURIDICO_EMPRESARIAL_ATUALIZADA.md`
  - `docs/DOCUMENTACAO_APIS_ENDPOINTS.md`
  - `docs/modulo-suporte.md`
  - `docs/guia-implementacao-suporte.md`
  - `docs/fluxo-modulo-suporte-atualizado.md`
  - `docs/fluxograma-modulo-suporte.md`
  - `docs/DOCUMENTACAO_FLUXO_CHAMADOS.md`
  - `docs/DOCUMENTACAO_CHAT_INTERNO.md`
  - `docs/ANALISE_CHAT_INTERNO_PROPOSTA.md`

## Padronizações
- Versões: Spring Boot 3.5.5; Front-end padrão documental Bootstrap 5.3.3, FA 6.4.0, jQuery 3.7.0.
- Artefato: `portal-ceo-0.0.1-SNAPSHOT.jar`.
- Template e Glossário adicionados.

## Índices Criados
- `docs/INDEX.md` (geral) e `docs/INDEX_APIS.md` (APIs por módulo).

## Referências de Código
- `src/main/resources/templates/chat/index.html:7` usa Bootstrap 5.3.0.
- `src/main/resources/templates/marketing/dashboard/index.html:14` usa FA 6.0.0.
- `src/main/resources/templates/juridico/documentos.html:264` usa Bootstrap 5.3.3.

## Próximos Ajustes Sugeridos
- Alinhar versões de front-end nos templates para o padrão adotado.
- Considerar adoção de OpenAPI (Springdoc) para gerar especificação oficial.
