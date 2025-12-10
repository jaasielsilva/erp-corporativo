# Documentação Técnica — RH/Configurações

## Arquitetura
- Controller: `RhConfiguracoesController` (`/rh/configuracoes/*`)
- Modelos: `RhPoliticaFerias`, `RhParametroPonto`, `RhIntegracaoConfig`
- Repositórios: `RhPoliticaFeriasRepository`, `RhParametroPontoRepository`, `RhIntegracaoConfigRepository`
- Templates: `templates/rh/configuracoes/*`
- Segurança: `@PreAuthorize('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')`
- Auditoria: `AuditoriaRhLogService`

## Endpoints MVC
- `GET /rh/configuracoes` → Página inicial com atalhos
  - Template: `src/main/resources/templates/rh/configuracoes/index.html`
- `GET /rh/configuracoes/politicas-ferias` → Form de políticas
  - Template: `src/main/resources/templates/rh/configuracoes/politicas-ferias.html`
- `POST /rh/configuracoes/politicas-ferias` → Salvar políticas
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhConfiguracoesController.java:66`
- `GET /rh/configuracoes/ponto` → Form de ponto
  - Template: `src/main/resources/templates/rh/configuracoes/ponto.html`
- `POST /rh/configuracoes/ponto` → Salvar ponto
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhConfiguracoesController.java:98`
- `GET /rh/configuracoes/integracoes` → Form de integrações
  - Template: `src/main/resources/templates/rh/configuracoes/integracoes.html`
- `POST /rh/configuracoes/integracoes` → Salvar integrações
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhConfiguracoesController.java:111`

## Regras de Negócio Aplicadas
- Férias:
  - Limite anual por `diasPorAno` (validação server-side) em `RhPoliticaFerias.java`.
  - Blackout por `periodosBlackout`: bloqueia intervalo solicitado.
  - Aprovação automática quando `exigeAprovacaoGerente=false`.
  - Implementação: `src/main/java/com/jaasielsilva/portalceo/service/rh/SolicitacaoFeriasService.java:86-122`.
- Ponto:
  - Correções: bloqueio conforme `permitirCorrecaoManual`.
  - Mensagens conforme exigência de aprovação.
  - Espelho de ponto (PDF) aplica parâmetros:
    - `toleranciaMinutos`: não considera atrasos diários dentro do limite.
    - `arredondamentoMinutos`: arredonda o total diário ao múltiplo.
    - `limiteHoraExtraDia`: limita extras diárias ao máximo.
  - Implementação: `src/main/java/com/jaasielsilva/portalceo/controller/rh/PontoEscalasController.java:508-522` e `src/main/java/com/jaasielsilva/portalceo/service/EspelhoPontoProfissionalService.java:304-340`.

## Modelos
- `RhPoliticaFerias` (`src/main/java/com/jaasielsilva/portalceo/model/RhPoliticaFerias.java`)
  - `diasPorAno` (`@Min(1) @Max(40)`), `permitirVenda`, `exigeAprovacaoGerente`, `periodosBlackout`.
- `RhParametroPonto` (`src/main/java/com/jaasielsilva/portalceo/model/RhParametroPonto.java`)
  - `toleranciaMinutos`, `arredondamentoMinutos`, `limiteHoraExtraDia`, `exigeAprovacaoGerente`, `permitirCorrecaoManual`.
- `RhIntegracaoConfig` (`src/main/java/com/jaasielsilva/portalceo/model/RhIntegracaoConfig.java`)
  - `folhaProvider`, `apiEndpointFolha`, `beneficiosProvider`, `apiEndpointBeneficios`, `emailNotificacoes`, `habilitarNotificacoes`.

## Auditoria
- Serviço: `src/main/java/com/jaasielsilva/portalceo/service/AuditoriaRhLogService.java`
- Salvamentos registram categoria `CONFIGURACAO` com recursos:
  - `/rh/configuracoes/politicas-ferias`
  - `/rh/configuracoes/ponto`
  - `/rh/configuracoes/integracoes`

## Segurança
- Acesso controlado via `@PreAuthorize('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')` no controller.
- Sidebar exibe entradas de menu condicionadas: `src/main/resources/templates/components/sidebar.html:290-300`.

## Deploy e Teste
- Build: `mvn -q -DskipTests package`
- Executar: `java -jar target/portal-ceo-0.0.1-SNAPSHOT.jar`
- Acessar: `http://localhost:8080/rh/configuracoes`

## Roadmap
- Aplicar tolerância e arredondamento nos cálculos de espelho de ponto.
- Habilitar notificações de integrações para eventos de aprovação/alteração.
