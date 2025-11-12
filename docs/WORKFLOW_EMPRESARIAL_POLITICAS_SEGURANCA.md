# Workflow Empresarial Completo — Políticas de Segurança

Este documento especifica, em nível empresarial e técnico, todo o fluxo de criação, revisão, aprovação, publicação, aceite e arquivamento de Políticas de Segurança no sistema. Deve ser utilizado como guia oficial para implementação, operação e auditoria.

## 1. Objetivo e Escopo

- Padronizar o ciclo de vida de políticas de segurança corporativas.
- Garantir trilha de auditoria (quem, quando, de onde) e conformidade.
- Disponibilizar APIs, páginas e processos para criação, aprovação e aceite de usuários.
- Escopo: módulo Termos/Políticas, páginas TI > Segurança, integrações com notificações e relatórios.

## 2. Papéis e Perfis (RBAC)

- `MASTER`: acesso total, decisão final, auditoria.
- `TI_ADMIN`: cria, envia para aprovação, publica e arquiva políticas.
- `COMPLIANCE`: aprova políticas, define regras e valida conformidade.
- `USER`/`VISITANTE`: lê, aceita quando obrigatório.

Regras de autorização recomendadas nos endpoints críticos:

- Enviar para aprovação: `ROLE_TI_ADMIN` ou `ROLE_MASTER`.
- Aprovar: `ROLE_COMPLIANCE`, `ROLE_TI_ADMIN` ou `ROLE_MASTER`.
- Publicar: `ROLE_TI_ADMIN` ou `ROLE_MASTER`.
- Arquivar: `ROLE_TI_ADMIN` ou `ROLE_MASTER`.

## 3. Estados e Transições (Termo.StatusTermo)

- `RASCUNHO` → criação inicial.
- `PENDENTE_APROVACAO` → enviado para aprovadores.
- `APROVADO` → aprovado formalmente, pronto para publicação.
- `PUBLICADO` → ativo e vigente; dispara aceite obrigatório (se marcado).
- `ARQUIVADO` → retirado de vigência; mantém histórico.
- `CANCELADO` → inválido; não será publicado (uso eventual).

Regras:
- Apenas `RASCUNHO` pode ir para `PENDENTE_APROVACAO`.
- Apenas `PENDENTE_APROVACAO` pode ir para `APROVADO`.
- Apenas `APROVADO` pode ir para `PUBLICADO`.
- `PUBLICADO` pode ir para `ARQUIVADO`.

## 4. Fluxo Empresarial Detalhado

1) Proposição (TI_ADMIN)
   - Criar rascunho da política com título, versão, conteúdo, se há aceite obrigatório e se deve notificar usuários.
   - Resultado: status `RASCUNHO`.

2) Revisão Interna (TI_ADMIN/COMPLIANCE)
   - Ajustes no rascunho; validação de conteúdo e escopo.
   - Enviar para aprovação.
   - Resultado: status `PENDENTE_APROVACAO`.

3) Aprovação Formal (COMPLIANCE)
   - Aprovar a política.
   - Sistema registra `aprovadoPor` e `dataAprovacao`.
   - Resultado: status `APROVADO`.

4) Publicação (TI_ADMIN)
   - Publicar a política.
   - Sistema registra `dataPublicacao` e atualiza contadores.
   - Se `obrigatorioAceite = true`, usuários devem aceitar.
   - Resultado: status `PUBLICADO` (vigente).

5) Aceite de Usuários (USER)
   - Usuário acessa política de segurança e aceita, com registro de `ip`, `userAgent` e `versaoTermo`.
   - Sistema atualiza estatísticas e pendências de aceite.

6) Monitoramento e Auditoria (TI/COMPLIANCE)
   - Acompanhar estatísticas, pendências de aceite, alertas de segurança e logs.

7) Arquivamento/Expiração (TI_ADMIN)
   - Arquivar quando substituída por nova versão ou fim de vigência.

## 5. API — Contratos, Regras e Exemplos

Base: `/termos/api`

### 5.1 Criar Política

- `POST /termos/api/politica-seguranca`
- Autenticação requerida. Tipo forçado: `POLITICA_SEGURANCA`.

Request body (JSON):
```json
{
  "titulo": "Política de Senhas",
  "versao": "v1.0",
  "conteudo": "Regras de complexidade e rotação de senhas.",
  "obrigatorioAceite": true,
  "notificarUsuarios": true,
  "observacoes": "Aplicável a todos os usuários"
}
```

Responses:
- 200: objeto `TermoDTO` criado em `RASCUNHO`.
- 400/401: erro de validação ou não autenticado.

### 5.2 Listar Políticas de Segurança

- `GET /termos/api/politicas`
- Retorna lista de políticas com tipo `POLITICA_SEGURANCA`.

### 5.3 Enviar para Aprovação

- `POST /termos/api/{id}/enviar-aprovacao`
- Regras: somente `RASCUNHO` pode ir para `PENDENTE_APROVACAO`.

### 5.4 Aprovar

- `POST /termos/api/{id}/aprovar`
- Registra `aprovadoPor` e `dataAprovacao`.

### 5.5 Publicar

- `POST /termos/api/{id}/publicar`
- Regras: somente `APROVADO` pode ir para `PUBLICADO`.
- Atualiza contadores de aceite.

### 5.6 Arquivar

- `POST /termos/api/{id}/arquivar`

### 5.7 Aceitar Termo (genérico)

- `POST /termos/aceitar/{id}`
- Registra aceite com contexto (`ip`, `userAgent`).

### 5.8 Códigos de Erro e Mensagens

- 400: validação, estado inválido, termo não encontrado.
- 401: não autenticado.
- 403: autenticado porém sem permissão (após reforço RBAC).
- 500: erro interno — deve ser logado com stacktrace.

## 6. Páginas e UX

### 6.1 TI > Segurança (`/ti/seguranca`)

- Exibe alertas de segurança e logs.
- Tabela de políticas (visualização) e atalho “Gerenciar Políticas”.

Padrão de layout obrigatório:
- `components/sidebar` incluído com `th:replace="~{components/sidebar :: sidebar}"`.
- `components/topbar` incluído com `th:replace="~{components/topbar :: topbar}"`.
- `components/footer` incluído com `th:replace="~{components/footer :: footer}"`.
- Uso do Bootstrap e fontes conforme templates existentes.

### 6.2 Gerenciar Políticas (`/termos/gerenciar`)

- Formulário de rascunho: título, versão, conteúdo, observações, flags de aceite/notificação.
- Ações por item: Enviar Aprovação, Aprovar, Publicar, Arquivar.
- Lista consome `GET /termos/api/politicas`.

Padrão de layout obrigatório:
- `components/sidebar`, `components/topbar` e `components/footer` com `th:replace`, igual ao TI > Segurança.
- Estrutura principal dentro de `<div class="app-container">` e `<main class="main-content">`.

### 6.3 Termos de Uso, Privacidade, Histórico e Aceites (`/termos/*`)

- `uso`: leitura e aceite (se aplicável).
- `privacidade`: leitura da política de privacidade.
- `historico`: todas as versões, ordenadas por data.
- `aceites`: estatísticas consolidadas.

## 7. Modelo de Dados

### 7.1 Termo

- Campos principais: `id`, `titulo`, `conteudo`, `versao`, `tipo`, `status`, `criadoPor`, `aprovadoPor`, `dataCriacao`, `dataAprovacao`, `dataPublicacao`, `dataVigenciaInicio`, `dataVigenciaFim`, `obrigatorioAceite`, `notificarUsuarios`, `observacoes`, contadores (`totalAceites`, etc.).
- `tipo` inclui: `POLITICA_SEGURANCA`, `TERMOS_USO`, `POLITICA_PRIVACIDADE`, etc.
- Índices: por `status`, `tipo`, `dataCriacao`.

### 7.2 TermoAceite

- Campos: `id`, `termo`, `usuario`, `dataAceite`, `ipAceite`, `userAgent`, `status`, `versaoTermo`.
- Índices: por `termo`, `usuario`, `status`, `dataAceite`.

## 8. Validações e Regras de Negócio

- Obrigatórios: `titulo`, `conteudo`, `versao`.
- `versao` única por política (recomendado).
- Transições de estado respeitam a máquina de estados (erros 400 em violações).
- `dataVigenciaInicio` ≤ `dataVigenciaFim` quando ambas definidas.
- Publicação só com `APROVADO` e conteúdo não vazio.

## 9. Segurança e Conformidade

- `anyRequest().authenticated()` ativo; CSRF desabilitado no ambiente atual (avaliar reativação).
- Reforço RBAC com `@PreAuthorize` nos endpoints críticos (aprovar, publicar, arquivar, enviar aprovação).
- Auditabilidade: logar usuário autenticado, IP, user agent nos eventos.
- Proteção a mass assignment: campos sensíveis (tipo/status) não devem ser alterados via payload em etapas indevidas.

## 10. Notificações

- Na publicação com `obrigatorioAceite = true`: notificar usuários-alvo (push/websocket/email).
- Na mudança para `PENDENTE_APROVACAO`: notificar aprovadores (COMPLIANCE/TI_ADMIN).
- Na aprovação: notificar solicitante/criador.

Templates mínimos:
- Título, resumo, link para política, versão, prazo (se houver), ações pendentes.

## 11. Auditoria e Logs

- Registrar no banco/histórico: criação, envio aprovação, aprovação, publicação, arquivamento, aceite.
- Capturar: `usuario`, `data/hora`, `ip`, `userAgent`, `status anterior → novo`.
- Retenção e exportação: CSV/PDF sob demanda.

## 12. Métricas e Relatórios

- Estatísticas: total termos, ativos, rascunhos, arquivados.
- Aceites: hoje, semana, mês; pendências por usuário.
- Políticas em aprovação: contagem e tempo médio de aprovação.

## 13. Testes — Plano de Qualidade

### 13.1 Unitários

- `TermoService`: transições válidas/invalidas, criação, publicação, contadores de aceite.
- `TermosController`: respostas 200/400/401.

### 13.2 Integração

- Fluxo completo: criar → enviar aprovação → aprovar → publicar → aceitar.
- RBAC: negar ações sem perfil.

### 13.3 E2E (UI)

- `gerenciar`: criar rascunho, enviar aprovação, aprovar, publicar, arquivar.
- `ti/seguranca`: atalho e exibição das políticas.

### 13.4 Critérios de Aceite do Projeto

- Todos os fluxos funcionam com perfis corretos e geram auditoria.
- Estatísticas refletem dados dos termos e aceites.
- Notificações disparam conforme regras.

## 14. Rollout e Migração

- Migrar políticas existentes para o novo fluxo (definir status coerente).
- Comunicar usuários sobre aceite obrigatório da nova política.
- Plano de reversão: possibilidade de arquivar e reverter publicação.

## 15. Versionamento e Substituição

- Nova versão (ex.: `v1.1`) publica e arquiva a anterior, mantendo histórico e aceites.
- `versao` usada para associar aceites à versão vigente.

## 16. Riscos e Mitigações

- Publicação sem aprovação: mitigar com RBAC e validação.
- Ausência de auditoria: mitigar com logs estruturados e histórico em banco.
- Falta de aceite obrigatório: checagem em login/áreas sensíveis.

## 17. RACI (Responsabilidade)

- Responsável: TI_ADMIN.
- Aprovador: COMPLIANCE.
- Consultado: MASTER.
- Informado: Usuários impactados.

## 18. Backlog de Melhorias

- Reativar CSRF e adequar chamadas (token).
- Notificações com templates corporativos e preferências por usuário.
- Relatórios avançados (tendência, SLA aprovação, expiração programada).

---

## Anexos — Exemplos de cURL

Criar rascunho:
```bash
curl -i -X POST "http://localhost:8080/termos/api/politica-seguranca" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=<sessao>" \
  -d '{
    "titulo": "Política de Senhas",
    "versao": "v1.0",
    "conteudo": "Regras de complexidade e rotação de senhas.",
    "obrigatorioAceite": true,
    "notificarUsuarios": true,
    "observacoes": "Aplicável a todos os usuários"
  }'
```

Enviar aprovação:
```bash
curl -i -X POST "http://localhost:8080/termos/api/1/enviar-aprovacao" -H "Cookie: JSESSIONID=<sessao>"
```

Aprovar:
```bash
curl -i -X POST "http://localhost:8080/termos/api/1/aprovar" -H "Cookie: JSESSIONID=<sessao>"
```

Publicar:
```bash
curl -i -X POST "http://localhost:8080/termos/api/1/publicar" -H "Cookie: JSESSIONID=<sessao>"
```

Arquivar:
```bash
curl -i -X POST "http://localhost:8080/termos/api/1/arquivar" -H "Cookie: JSESSIONID=<sessao>"
```

Listar políticas:
```bash
curl -s "http://localhost:8080/termos/api/politicas" -H "Cookie: JSESSIONID=<sessao>" | jq
```