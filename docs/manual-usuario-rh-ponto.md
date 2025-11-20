# Manual do Usuário – RH: Folha de Pagamento e Ponto/Escalas

## Objetivo
- Orientar o uso das páginas e botões para executar e testar os fluxos de Folha de Pagamento e Ponto/Escalas com dados reais.
- Cobrir entradas exigidas, ações, resultados esperados e critérios de sucesso.

## Perfis e Acesso
- Acesso controlado por perfil: `ROLE_ADMIN`, `ROLE_MASTER`, `ROLE_RH`, `ROLE_GERENCIAL`.
- Se não possuir o perfil, solicite ao administrador a inclusão.

---

## Folha de Pagamento

### Página: Gerar Folha
- Caminho: `/rh/folha-pagamento/gerar`
- Objetivo: criar a folha do período e calcular salários, proventos e descontos.

**Campos**
- `Mês` e `Ano` de referência.

**Botões**
- `Calcular` – cria e processa a folha (`POST /rh/folha-pagamento/processar`), define status `EM_PROCESSAMENTO` e, ao concluir, `PROCESSADA`.
- `Revisar` – acessa a visualização da folha (`GET /rh/folha-pagamento/visualizar/{id}`) para conferência.
- `Gerar Holerites` – navega para holerites (`GET /rh/folha-pagamento/holerite`).
- `Aprovar` – fecha a folha (`POST /rh/folha-pagamento/fechar/{id}`), disponível só quando `PROCESSADA`, consolidando `FECHADA`.

**Teste funcional completo**
1. Selecionar `Mês` e `Ano` válidos e clicar `Calcular`.
2. Confirmar que o botão `Revisar` fica ativo e abre o resumo com proventos/descontos.
3. Acessar `Gerar Holerites` e emitir um holerite em PDF para um colaborador.
4. Retornar e clicar `Aprovar` (somente se status estiver `PROCESSADA`).
5. Validar que a folha muda para `FECHADA` e botões ficam bloqueados conforme regras.

Referências de código:
- `src/main/resources/templates/rh/folha-pagamento/gerar.html`
- `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java`
- `src/main/java/com/jaasielsilva/portalceo/service/FolhaPagamentoService.java`

---

## Ponto e Escalas

### Página: Registros de Ponto
- Caminho: `/rh/ponto-escalas/registros`
- Objetivo: validar matrícula e registrar eventos de ponto (Entrada/Saída/Retorno) do dia.

**Campos**
- `Matrícula do Funcionário` – vinculada ao usuário do colaborador.
- `Senha` – campo presente; a validação atual usa a matrícula.

**Elementos**
- Relógio em tempo real.
- Badge de status (mensagem dinâmica conforme validação e registro).
- Lista “Últimos Registros” do dia, carregada automaticamente.

**Botões/Ações**
- Ao sair do campo `Matrícula`, ocorre validação: `POST /rh/ponto-escalas/registros/validar-matricula`.
  - Resposta exibe `Colaborador` e `Próximo` tipo esperado.
- `Registrar Ponto`: `POST /rh/ponto-escalas/registros/registrar-por-matricula`.
  - Registra o próximo evento do dia (Entrada → Saída → Retorno → Saída).
- Lista: `GET /rh/ponto-escalas/registros/ultimos`.

**Teste funcional completo**
1. Digitar matrícula ativa e aguardar validação (badge exibe nome e próximo tipo).
2. Clicar `Registrar Ponto` e confirmar mensagem de sucesso com horário.
3. Verificar que o registro aparece na lista “Últimos Registros”.
4. Repetir para cobrir o ciclo Entrada/Saída/Retorno/Saída ao longo do dia.

Referências de código:
- `src/main/resources/templates/rh/ponto-escalas/registros.html`
- `src/main/java/com/jaasielsilva/portalceo/controller/rh/PontoEscalasController.java`
- `src/main/java/com/jaasielsilva/portalceo/repository/RegistroPontoRepository.java`
- `src/main/java/com/jaasielsilva/portalceo/model/RegistroPonto.java`

### Página: Escalas de Trabalho
- Caminho: `/rh/ponto-escalas/escalas`
- Objetivo: visualizar e preparar a gestão de escalas por mês/departamento/turno (dados reais no seletor).

**Seletor de Período e Filtros**
- `Mês`: preenchido dinamicamente (Thymeleaf) com mês atual selecionado.
- `Departamento`: lista real fornecida pelo backend.
- `Turno`: lista de `Escalas Vigentes` reais fornecida pelo backend.

**Teste funcional básico**
1. Abrir a página e confirmar que `Mês Atual`, `Departamentos` e `Escalas Vigentes` carregam com dados reais.
2. Alterar filtros e observar atualizações visuais (os blocos do calendário existem; a atribuição persistente será adicionada em etapa posterior).

Referências de código:
- `src/main/resources/templates/rh/ponto-escalas/escalas.html`
- `src/main/java/com/jaasielsilva/portalceo/controller/rh/PontoEscalasController.java`
- `src/main/java/com/jaasielsilva/portalceo/model/EscalaTrabalho.java`
- `src/main/java/com/jaasielsilva/portalceo/repository/EscalaTrabalhoRepository.java`

### Página: Correções de Ponto
- Caminho: `/rh/ponto-escalas/correcoes`
- Objetivo: solicitar correções de registros (atraso, falta, ajuste de horário etc.).

**Campos**
- `Colaborador` – lista real integrada.
- `Data da Correção`.
- `Tipo de Correção` – atraso, falta, hora extra, ajuste de horário, registro perdido.
- `Horários` – exibidos conforme tipo.
- `Justificativa` e `Anexos`.

**Botões**
- `Nova Correção` – abre modal.
- `Solicitar Correção` – envia para o endpoint interno `POST /rh/ponto-escalas/correcoes/solicitar`.
- `Aprovar/Rejeitar Selecionadas` – ações visuais para teste de fluxo.

**Teste funcional completo**
1. Abrir `Nova Correção`, selecionar colaborador e tipo.
2. Preencher horários quando aplicável e justificativa.
3. Clicar `Solicitar Correção` e validar mensagem de sucesso.

Referências de código:
- `src/main/resources/templates/rh/ponto-escalas/correcoes.html`
- `src/main/java/com/jaasielsilva/portalceo/controller/rh/PontoEscalasController.java`

### Página: Relatórios de Ponto (Espelho)
- Caminho: `/rh/ponto-escalas/relatorios`
- Objetivo: gerar e baixar PDF do espelho de ponto por colaborador e mês.

**Campos**
- `Matrícula` – validar antes de gerar.
- `Mês` e `Ano`.

**Botões/Ações**
- `Validar Colaborador` – `POST /rh/ponto-escalas/registros/validar-matricula`.
- `Gerar e Baixar PDF` – `GET /rh/ponto-escalas/relatorio-mensal/pdf`.

**Teste funcional completo**
1. Informar a matrícula e validar (exibe nome e libera o botão de gerar). 
2. Selecionar `Mês` e `Ano` e clicar `Gerar e Baixar PDF`.
3. Abrir o PDF e conferir por dia: entradas/saídas, total e status.

Referências de código:
- `src/main/resources/templates/rh/ponto-escalas/relatorios.html`
- `src/main/resources/templates/rh/ponto-escalas/espelho-ponto-pdf.html`
- `src/main/java/com/jaasielsilva/portalceo/controller/rh/PontoEscalasController.java`

---

## Cenários de Teste Sugeridos

### Folha de Pagamento
- Calcular folha com mês/ano sem folha existente – esperar criação e status `PROCESSADA`.
- Revisar folha – conferir totais e itens de proventos/descontos.
- Gerar holerites – emitir PDF para um colaborador e conferir valores.
- Aprovar folha – apenas quando `PROCESSADA`, esperar `FECHADA`.

### Registros de Ponto
- Matrícula válida e ativa – badge mostra “Próximo” tipo esperado.
- Matrícula inválida/inativa – exibe mensagem de erro e desabilita submit.
- Sequência de registros – Entrada, Saída, Retorno, Saída; cada ação atualiza a lista.

### Correções
- Solicitar correção com campos obrigatórios – resposta de sucesso.
- Solicitar sem obrigatórios – mensagem de validação.

### Relatórios
- Validar matrícula e gerar PDF – download concluído e dados consistentes com registros do mês.

---

## Dicas e Solução de Problemas
- Se a validação de matrícula falhar, garanta que o `Usuario` do colaborador possui `matricula` e que o colaborador está `ATIVO`.
- Para o PDF, se abrir em branco, verifique se o colaborador possui registros no mês selecionado.
- Compile o projeto após alterações: `mvn -q -DskipTests compile`.

---

## Contatos
- Em caso de dúvidas ou falhas, acione o responsável de TI ou RH.