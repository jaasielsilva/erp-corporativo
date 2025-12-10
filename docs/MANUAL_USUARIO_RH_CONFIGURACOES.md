# Manual do Usuário — RH/Configurações

## Acesso
- `Início`: `http://localhost:8080/rh/configuracoes`
- `Políticas de Férias`: `http://localhost:8080/rh/configuracoes/politicas-ferias`
- `Parâmetros de Ponto`: `http://localhost:8080/rh/configuracoes/ponto`
- `Integrações`: `http://localhost:8080/rh/configuracoes/integracoes`

## Perfis com acesso
- `ADMIN`, `MASTER`, `RH_GERENTE` (páginas e salvamentos)

## Políticas de Férias
- Campos:
  - `Dias por Ano`: limite anual de férias.
  - `Permitir Venda de Férias`: habilita venda conforme política.
  - `Exige Aprovação do Gerente`: se desmarcado, solicitações são aprovadas automaticamente.
  - `Períodos de Blackout`: períodos bloqueados (formato `MM-dd;MM-dd;...`).
- Ações:
  - `Salvar`: persiste política e registra auditoria.
- Regras ativas no módulo:
  - Solicitações que coincidirem com blackout: bloqueadas.
  - Solicitações com dias acima do limite: bloqueadas.
  - Aprovação automática se não exigir gerente.
 - Validações automáticas após salvar:
   - Limites e formatos (`@Min/@Max` em `diasPorAno`, formato de blackout).
   - Auditoria: registro de alteração em `AuditoriaRhLogService`.
 - Verificação prática:
   - Criar uma solicitação em `RH → Férias → Solicitar` e observar bloqueios/aprovação automática conforme política.
   - Consultar MySQL: `SELECT * FROM rh_politicas_ferias ORDER BY id DESC LIMIT 1;`.

## Parâmetros de Ponto
- Campos:
  - `Tolerância (minutos)`, `Arredondamento (minutos)`, `Limite Hora Extra/dia`.
  - `Exige Aprovação do Gerente`: exige aprovação para correções.
  - `Permitir Correção Manual`: bloqueia ou libera correção manual.
- Ações:
  - `Salvar`: persiste parâmetros e registra auditoria.
- Regras ativas no módulo:
  - Correções bloqueadas quando `Permitir Correção Manual` = Não.
  - Mensagem de fluxo conforme exigência de aprovação.
  - Espelho de ponto aplica:
    - `Tolerância (minutos)`: déficits até o limite não geram atraso diário.
    - `Arredondamento (minutos)`: total diário arredondado ao múltiplo definido.
    - `Limite Hora Extra/dia`: extras diárias limitadas ao máximo configurado.
 - Validações automáticas após salvar:
   - Limites (`@Min/@Max`) e consistência dos parâmetros.
   - Auditoria: registro de alteração em `AuditoriaRhLogService`.
 - Verificação prática:
   - Gerar PDF do espelho em `RH → Ponto e Escalas → Relatórios` e verificar aplicação de tolerância/arredondamento/limite.
   - Consultar MySQL: `SELECT * FROM rh_parametros_ponto ORDER BY id DESC LIMIT 1;`.

## Integrações
- Campos:
  - `Provedor de Folha`, `Endpoint API Folha`.
  - `Provedor de Benefícios`, `Endpoint API Benefícios`.
  - `Email de Notificações`, `Habilitar Notificações`.
- Ações:
  - `Salvar`: persiste integrações e registra auditoria.
 - Validações automáticas após salvar:
   - Formatos (tamanhos, `@Email`), presença de URL.
   - Auditoria: registro de alteração em `AuditoriaRhLogService`.
 - Verificação prática:
   - Testar rotinas que consomem esses endpoints (exportações/integrações) e observar logs.
   - Consultar MySQL: `SELECT * FROM rh_integracoes_config ORDER BY id DESC LIMIT 1;`.

## Passo a Passo
1. Acesse `RH > Configurações RH`.
2. Entre em cada página, ajuste os campos conforme política da empresa.
3. Clique em `Salvar` para aplicar.
4. Valide em Férias/Ponto a aplicação das regras (bloqueios, mensagens).

## Mensagens e Auditoria
- Sucesso ao salvar: mensagem verde no topo da página.
- Auditoria registrada com categoria `CONFIGURACAO` no backend.

## Dicas
- Configure blackout de fim de ano com `12-20;12-31`.
- Habilite `Habilitar Notificações` para futuros alertas por email.
