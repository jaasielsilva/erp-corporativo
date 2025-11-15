# Documentação Completa – Módulo Jurídico

Esta documentação descreve o que será implementado no módulo Jurídico do ERP Corporativo, incluindo objetivos, páginas, casos de uso, integrações com banco de dados, endpoints, regras de negócio, permissões e plano de implementação.

## 1. Visão Geral
- Objetivo: centralizar a gestão jurídica (contratos, processos, compliance e documentos) com dados reais do banco, oferecendo indicadores, produtividade e conformidade.
- Benefícios: redução de riscos, acompanhamento de prazos, visão executiva, padronização de fluxos e auditoria de ações.
- Público: equipe jurídica, compliance, diretoria e gestores com permissão.

## 2. Escopo de Páginas do Módulo
As páginas já existem (estáticas) e serão integradas com dados reais:
- Dashboard (`/juridico`): indicadores e atalhos.
- Contratos (`/juridico/contratos`): listagem, filtros, detalhes e ações.
- Processos (`/juridico/processos`): processos judiciais/administrativos, prazos e audiências.
- Compliance (`/juridico/compliance`): normas, não conformidades, auditorias e status.
- Documentos (`/juridico/documentos`): biblioteca e modelos jurídicos, upload/assinaturas.

## 3. Perfis e Permissões
- Controle de acesso via atributos já existentes:
  - `podeAcessarJuridico` (GlobalControllerAdvice): permite acesso para Jurídico, Gerencial, Master e Admin.
  - `isJuridico`: detecta cargos relacionados (jurídico, advogado, legal, compliance).
- Regras por página:
  - Visualização: `podeAcessarJuridico`.
  - Criação/Edição: `isJuridico` ou perfis elevados (Admin/Master) conforme regra do caso.

## 4. Dados Reais e Modelagem
### 4.1 Contratos
- Entidade existente: `ContratoLegal` (JPA) com repositório `ContratoLegalRepository` e serviço `ContratoLegalService`.
- Campos relevantes: número, título, descrição, tipo, status, prioridade, data início/fim/vencimento, valor, contraparte (cliente/fornecedor), aditivos, alertas, responsável, etc.
- Consultas úteis: por `status`, `tipo`, vencendo, vencidos, estatísticas gerais e por tipo/usuário.

### 4.2 Processos Jurídicos
- Novas entidades propostas:
  - `ProcessoJuridico`: número, tipo, assunto, tribunal/órgão, partes, status (rascunho/andamento/arquivado), responsável, datas, valor causa, classe, comarca, vara.
  - `Audiencia`: processo, data/hora, local/vara, tipo, status, observações.
  - `PrazoJuridico`: processo, descrição, data limite, responsável, status (pendente/concluído), prioridade.
  - `AndamentoProcesso`: processo, data, descrição, usuário responsável.
- Repositórios/Serviços: `ProcessoJuridicoRepository`, `AudienciaRepository`, `PrazoJuridicoRepository`, `AndamentoProcessoRepository` + services correspondentes.

### 4.3 Compliance
- Novas entidades propostas:
  - `Norma`: título, categoria (LGPD, trabalhista, fiscal, etc.), descrição, status (vigente/obsoleta), referências.
  - `NaoConformidade`: título, descrição, origem (auditoria/processo/contrato), severidade, responsável, status, datas.
  - `AuditoriaCompliance`: tipo, escopo, datas, auditor/responsável, achados, plano de ação, status.
- Repositórios/Serviços: `NormaRepository`, `NaoConformidadeRepository`, `AuditoriaComplianceRepository` + serviços.

### 4.4 Documentos
- Integração sugerida:
  - `DocumentoJuridico`: título, categoria, descrição, caminho arquivo, status, autor, data upload, vinculações (contrato/processo/compliance).
  - Upload para `uploads/juridico/documentos/` com controle de permissões, versão e assinatura.
  - Repositório/Serviço: `DocumentoJuridicoRepository` + `DocumentoJuridicoService`.

## 5. Endpoints e Rotas
### 5.1 Views (Thymeleaf)
- `GET /juridico` → `juridico/index` (Dashboard com dados dinâmicos)
- `GET /juridico/contratos` → `juridico/contratos`
- `GET /juridico/processos` → `juridico/processos`
- `GET /juridico/compliance` → `juridico/compliance`
- `GET /juridico/documentos` → `juridico/documentos`

### 5.2 APIs (JSON)
- Dashboard:
  - `GET /juridico/api/dashboard/estatisticas` → métricas em tempo real (contratos/processos/prazos/compliance).
- Contratos (usar dados reais de `ContratoLegal`):
  - `GET /juridico/api/contratos?status=&tipo=&page=&size=`
  - `GET /juridico/api/contratos/{id}`
  - `POST /juridico/api/contratos` (criar)
  - `PUT /juridico/api/contratos/{id}` (atualizar)
  - `DELETE /juridico/api/contratos/{id}` (excluir, conforme regra)
  - `GET /juridico/api/contratos/estatisticas` (resumo, valores, vencimentos)
- Processos:
  - `GET /juridico/api/processos?status=&search=&page=&size=`
  - `GET /juridico/api/processos/{id}`
  - `POST /juridico/api/processos` (criar)
  - `PUT /juridico/api/processos/{id}` (atualizar)
  - `POST /juridico/api/processos/{id}/audiencias` (criar audiência)
  - `POST /juridico/api/processos/{id}/prazos` (criar prazo)
- Compliance:
  - `GET /juridico/api/compliance/status` (indicadores)
  - `GET /juridico/api/compliance/normas`
  - `GET /juridico/api/compliance/nao-conformidades`
  - `GET /juridico/api/compliance/auditorias`
  - `POST /juridico/api/compliance/auditorias` (criar)
- Documentos:
  - `GET /juridico/api/documentos?categoria=&page=&size=`
  - `POST /juridico/api/documentos/upload` (upload)
  - `GET /juridico/api/documentos/{id}`
  - `DELETE /juridico/api/documentos/{id}`

## 6. Casos de Uso
### 6.1 Dashboard
- Ver métricas de contratos ativos, processos em andamento, prazos vencendo e alertas de compliance.
- Acesso rápido às seções: contratos, processos, compliance e documentos.

### 6.2 Contratos
- Listar contratos por status/tipo e ordenar por valor/datas.
- Consultar detalhes do contrato e aditivos/alertas.
- Criar/editar/ativar/assinar/renovar/suspender/encerrar conforme fluxo e permissões.
- Ver contratos vencendo nos próximos X dias.
- Estatísticas: total por tipo/status, valor total/médio, vencidos/vencendo, performance por responsável.

### 6.3 Processos
- Criar processo com dados do tribunal, partes e assunto.
- Adicionar audiência e prazos com responsáveis e notificações.
- Registrar andamentos e anexos.
- Filtrar por status (ativo/andamento/arquivado) e busca textual.
- Acompanhar prazos críticos e audiências próximas.

### 6.4 Compliance
- Listar normas vigentes e histórico.
- Gerenciar não conformidades: abrir, classificar, priorizar, atribuir, encerrar.
- Planejar e registrar auditorias: tipo, escopo, achados e plano de ação.
- Ver indicadores: conformidade por área, severidade média, ações pendentes.

### 6.5 Documentos
- Upload de documentos jurídicos com categorização.
- Vincular documento a contrato/processo.
- Controlar status (ativo/pendente/assinado), versão e autor.
- Remover ou arquivar, conforme regra e permissão.

## 7. Como Usar (Fluxos de Navegação)
### 7.1 Acesso ao Módulo
1. Abrir `/juridico` no sistema.
2. Garantir que o usuário tenha `podeAcessarJuridico` ou seja `isJuridico`.

### 7.2 Dashboard
1. Usar os cards e atalhos.
2. Indicadores atualizam via `GET /juridico/api/dashboard/estatisticas` periodicamente.

### 7.3 Contratos
1. Acessar `/juridico/contratos`.
2. Usar filtros (status/tipo/busca) que consultam `ContratoLegalRepository`.
3. Abrir detalhes e executar ações (ativar/renovar/suspender/etc.).
4. Consultar estatísticas em `/juridico/api/contratos/estatisticas`.

### 7.4 Processos
1. Acessar `/juridico/processos`.
2. Criar processo via `POST /juridico/api/processos`.
3. Cadastrar audiência e prazos com endpoints específicos.
4. Monitorar prazos críticos e audiências próximas.

### 7.5 Compliance
1. Acessar `/juridico/compliance`.
2. Visualizar status e listar normas/não conformidades/auditorias.
3. Abrir auditoria via `POST /juridico/api/compliance/auditorias`.

### 7.6 Documentos
1. Acessar `/juridico/documentos`.
2. Fazer upload via `POST /juridico/api/documentos/upload`.
3. Visualizar, vincular e gerenciar documentos.

## 8. Regras de Negócio e Validações
- Contratos:
  - Número único (`existsByNumeroContrato`).
  - Status e transições válidas (ex.: não assinar contrato já assinado).
  - Renovação automática respeita `prazoNotificacao` e envio de alertas.
- Processos:
  - Número único por órgão/vara.
  - Prazos não podem ser retroativos; conclusão exige justificativa.
  - Audiência só pode ser criada para processo existente.
- Compliance:
  - Não conformidades exigem severidade e responsável.
  - Auditoria precisa de tipo e escopo claros.
- Documentos:
  - Tipos permitidos (PDF, DOCX) e limite de tamanho.
  - Controle de versão e assinatura digital quando aplicável.

## 9. Segurança, LGPD e Auditoria
- Permissões baseadas em perfil: acesso restrito ao módulo.
- Registro de ações (quem, quando, o que) em auditoria; integração com `TermoAuditoria` quando aplicável.
- Logs de eventos críticos (criação/alteração/remoção).
- Proteção de dados pessoais e contratos sensíveis; criptografia em trânsito (HTTPS) e revisão de escopos de acesso.

## 10. Integração com Banco de Dados
- JPA/Hibernate com entidades novas e existentes.
- Paginação (`Pageable`) para listas grandes.
- Índices: número de contrato/processo, datas críticas, status.
- Transações: serviços de escrita com `@Transactional`.

## 11. UI/UX e Renderização
- Thymeleaf para render dinâmico; componentes compartilhados (sidebar/topbar/footer).
- Dashboard com atualização periódica via fetch para estatísticas.
- Tabelas com paginação, filtros e ordenação.
- Badges de status e indicadores visuais de prazo/risco.

## 12. Testes e Observabilidade
- Testes unitários: serviços e repositórios (Contratos e novos módulos).
- Testes de integração: endpoints REST.
- Métricas: contadores de entidades, prazos críticos, estatísticas mensais.
- Logs estruturados e rastreabilidade.

## 13. Plano de Implementação (Fases)
1. Contratos: integrar páginas com `ContratoLegalService/Repository`, substituir dados estáticos e adicionar filtros/estatísticas.
2. Processos: criar entidades/repositórios/serviços e integrar página com listas, prazos e audiências.
3. Compliance: criar entidades e integrar com indicadores e listagens.
4. Documentos: criar entidade e upload, listar e vincular a contratos/processos.
5. Dashboard: consolidar indicadores de todas as áreas com dados reais.
6. Permissões e auditoria: revisar regras e registrar ações.
7. Testes: cobrir serviços, repositórios e APIs.

## 14. Considerações Técnicas
- Reutilizar serviços existentes quando possível (ex.: `ContratoLegalService`).
- Evitar consultas pesadas no render inicial; preferir APIs assíncronas para estatísticas.
- Padronizar DTOs para APIs e usar mapeadores.
- Manter consistência com nomenclatura e estrutura de outros módulos.

---
Última revisão: preparar integração das páginas do módulo com dados reais e iniciar pelas áreas com domínio já modelado (Contratos), evoluindo para Processos/Compliance/Documentos conforme especificado.