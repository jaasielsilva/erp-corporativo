# FLUXO CONFIG-RH

## Guia Rápido (para qualquer usuário)
- Pré-requisitos:
  - Estar logado com perfil `ADMIN`, `MASTER` ou `RH_GERENTE`.
  - Acessar: `/rh/configuracoes`.
  - Tempo estimado: ~5 min por página.
- Como testar:
  - Na página Início, clique em `Abrir` no card desejado.
  - Preencha os valores do Cenário Sucesso e clique `Salvar`.
  - Verifique a mensagem verde de sucesso e se os valores permanecem ao recarregar.
  - Repita com os valores do Cenário Erro e confirme que não aparece mensagem de sucesso.

## Visão Geral
- Módulo: `Configurações RH` (`/rh/configuracoes`)
- Páginas: Início, Políticas de Férias, Parâmetros de Ponto, Integrações
- Resultado esperado: salvar configurações, exibir mensagem de sucesso, manter valores persistidos e registrar auditoria

## Fluxo de Navegação
- Início `/rh/configuracoes`
  -> Selecionar card `Políticas de Férias`
  -> Abrir `/rh/configuracoes/politicas-ferias`
- Início `/rh/configuracoes`
  -> Selecionar card `Parâmetros de Ponto`
  -> Abrir `/rh/configuracoes/ponto`
- Início `/rh/configuracoes`
  -> Selecionar card `Integrações`
  -> Abrir `/rh/configuracoes/integracoes`

## Fluxo: Políticas de Férias
- Abrir `/rh/configuracoes/politicas-ferias`
  -> Preencher campos
    - Dias por Ano: número [1..40]
    - Permitir Venda de Férias: select {Sim, Não}
    - Exige Aprovação do Gerente: select {Sim, Não}
    - Períodos de Blackout: texto `MM-dd;MM-dd;...`
  -> Clicar `Salvar`
  -> Resultado esperado
    - Exibe alerta sucesso: "Políticas de férias salvas com sucesso"
    - Valores permanecem no formulário ao recarregar
    - Auditoria registrada categoria `CONFIGURACAO` ação `SALVAR_POLITICAS_FERIAS`
  -> Caso inválido
    - Dias por Ano fora de faixa (0, 41, negativos) -> falha validação backend
    - Blackout formato inválido ou muito longo -> falha de persistência/validação
    - Não exibir `successMessage`

### Dados de Teste
- Cenário Sucesso
  -> Dias por Ano: `30`
  -> Permitir Venda de Férias: `Sim`
  -> Exige Aprovação do Gerente: `Não`
  -> Períodos de Blackout: `12-20;01-05`
  -> Resultado: alerta de sucesso, valores persistidos, autoaprovação refletida nas férias (política aplicada)
- Cenário Erro
  -> Dias por Ano: `0`
  -> Permitir Venda de Férias: `Não`
  -> Exige Aprovação do Gerente: `Sim`
  -> Períodos de Blackout: `12/20;01/05` (formato inválido)
  -> Resultado: falha de validação/persistência no backend, sem `successMessage`

### Checklist de Resultado
- [ ] Sucesso: mensagem verde exibida
- [ ] Sucesso: valores mantidos ao recarregar
- [ ] Sucesso: auditoria registrada
- [ ] Erro: sem `successMessage`
- Observações: ______________________________

## Fluxo: Parâmetros de Ponto
- Abrir `/rh/configuracoes/ponto`
  -> Preencher campos
    - Tolerância (minutos): número [0..120]
    - Arredondamento (minutos): número [0..120]
    - Limite Hora Extra/dia: número [0..600]
    - Exige Aprovação do Gerente: select {Sim, Não}
    - Permitir Correção Manual: select {Sim, Não}
  -> Clicar `Salvar`
  -> Resultado esperado
    - Exibe alerta sucesso: "Parâmetros de ponto salvos com sucesso"
    - Valores permanecem no formulário ao recarregar
    - Auditoria registrada categoria `CONFIGURACAO` ação `SALVAR_PARAMETROS_PONTO`
  -> Caso inválido
    - Valores fora de faixa (-1, 121, 601) -> falha validação backend
    - Não exibir `successMessage`

### Dados de Teste
- Cenário Sucesso
  -> Tolerância (minutos): `15`
  -> Arredondamento (minutos): `5`
  -> Limite Hora Extra/dia: `300`
  -> Exige Aprovação do Gerente: `Sim`
  -> Permitir Correção Manual: `Não`
  -> Resultado: alerta de sucesso, parâmetros aplicados e persistidos
- Cenário Erro
  -> Tolerância (minutos): `-1`
  -> Arredondamento (minutos): `121`
  -> Limite Hora Extra/dia: `601`
  -> Exige Aprovação do Gerente: `Não`
  -> Permitir Correção Manual: `Sim`
  -> Resultado: falha de validação backend, sem `successMessage`

### Checklist de Resultado
- [ ] Sucesso: mensagem verde exibida
- [ ] Sucesso: valores mantidos ao recarregar
- [ ] Sucesso: auditoria registrada
- [ ] Erro: sem `successMessage`
- Observações: ______________________________

## Fluxo: Integrações
- Abrir `/rh/configuracoes/integracoes`
  -> Preencher campos
    - Provedor de Folha: texto (até 100 chars)
    - Endpoint API Folha: URL (até 200 chars)
    - Provedor de Benefícios: texto (até 200 chars)
    - Endpoint API Benefícios: URL (até 200 chars)
    - Email de Notificações: email (até 120 chars)
    - Habilitar Notificações: select {Sim, Não}
  -> Clicar `Salvar`
  -> Resultado esperado
    - Exibe alerta sucesso: "Integrações salvas com sucesso"
    - Valores permanecem no formulário ao recarregar
    - Auditoria registrada categoria `CONFIGURACAO` ação `SALVAR_INTEGRACOES`
  -> Caso inválido
    - Email inválido, URLs sem protocolo, textos acima de limite -> falha validação backend
    - Não exibir `successMessage`

### Dados de Teste
- Cenário Sucesso
  -> Provedor de Folha: `FolhaX`
  -> Endpoint API Folha: `https://api.folhax.com/v1`
  -> Provedor de Benefícios: `BenefX`
  -> Endpoint API Benefícios: `https://api.benefx.com/v1`
  -> Email de Notificações: `qa@empresa.com`
  -> Habilitar Notificações: `Sim`
  -> Resultado: alerta de sucesso, integracões persistidas
- Cenário Erro
  -> Provedor de Folha: texto com `101` caracteres
  -> Endpoint API Folha: `api.folhax.com/v1` (sem protocolo)
  -> Provedor de Benefícios: texto com `201` caracteres
  -> Endpoint API Benefícios: `htp://api.benefx` (URL inválida)
  -> Email de Notificações: `qa empresa.com` (email inválido)
  -> Habilitar Notificações: `Não`
  -> Resultado: falhas de validação backend (`@Size/@Email`), sem `successMessage`

### Checklist de Resultado
- [ ] Sucesso: mensagem verde exibida
- [ ] Sucesso: valores mantidos ao recarregar
- [ ] Sucesso: auditoria registrada
- [ ] Erro: sem `successMessage`
- Observações: ______________________________

## Setas de Fluxo Resumidas
- `/rh/configuracoes` -> `Políticas de Férias` -> preencher campos -> `Salvar` -> sucesso + persistência + auditoria
- `/rh/configuracoes` -> `Parâmetros de Ponto` -> preencher campos -> `Salvar` -> sucesso + persistência + auditoria
- `/rh/configuracoes` -> `Integrações` -> preencher campos -> `Salvar` -> sucesso + persistência + auditoria

## Resultados Esperados Globais
- Mensagem de sucesso visível após salvar
- Dados persistidos e recarregados no formulário
- Auditoria criada com categoria `CONFIGURACAO` e recurso da página
- Sem redirecionamento; renderiza o mesmo template com estado atualizado

## Observações
- Validações do backend aplicam limites e formatos (Bean Validation)
- Formularios usam `novalidate`; o navegador não bloqueia envio
- Em falhas de validação, esperar ausência de `successMessage` e erro no backend
