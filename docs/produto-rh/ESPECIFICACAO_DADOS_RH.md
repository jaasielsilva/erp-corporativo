# Especificação de Dados — Módulo RH

## Colaborador
- Campos e validações (origem `src/main/java/com/jaasielsilva/portalceo/model/Colaborador.java`):
  - `nome` (string, obrigatório, 2–100) — `.../Colaborador.java:44-47`
  - `cpf` (string, obrigatório, único, formato válido) — `.../Colaborador.java:49-52`
  - `email` (string, obrigatório, único, formato válido) — `.../Colaborador.java:54-57`
  - `telefone` (string, formato `^(\([1-9]{2}\) (?:[2-8]|9[1-9])[0-9]{3}-[0-9]{4})$`) — `.../Colaborador.java:59`
  - `sexo` (enum: MASCULINO, FEMININO, OUTRO) — `.../Colaborador.java:62-64,173-175`
  - `dataNascimento` (date) — `.../Colaborador.java:65-67`
  - `dataAdmissao` (date, obrigatório, passado/presente) — `.../Colaborador.java:68-72`
  - `estadoCivil` (enum: SOLTEIRO, CASADO, DIVORCIADO, VIUVO) — `.../Colaborador.java:74-76,177-179`
  - `status` (enum: ATIVO, INATIVO, SUSPENSO) — `.../Colaborador.java:77-79,181-183`
  - `salario` (decimal, >0, máx 8 inteiros + 2 decimais) — `.../Colaborador.java:106-109`
  - Endereço: `cep` (regex CEP), `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `estado`, `pais` — `.../Colaborador.java:112-121`
  - `cargaHoraria` (int, 1–60 horas semanais) — `.../Colaborador.java:127-129`
  - Relacionamentos: `cargo`, `departamento`, `beneficios`, `supervisor`, `usuario`, `historico` — `.../Colaborador.java:87-99,136-142`
- Exemplos:
  - Válido: nome "Ana Silva", cpf "123.456.789-09", email "ana@empresa.com", cep "01001-000", cargaHoraria 40.
  - Inválido: nome "A" (curto), cpf "123" (formato inválido), email "ana@", cargaHoraria 0.

## Registro de Ponto
- Campos (origem `src/main/java/com/jaasielsilva/portalceo/model/RegistroPonto.java`):
  - `data` (date, obrigatório), entradas/saídas 1–4 — `.../RegistroPonto.java:29-55`
  - Totais: `totalMinutosTrabalhados`, `totalMinutosIntervalo`, `minutosAtraso`, `minutosHoraExtra` — `.../RegistroPonto.java:56-67`
  - `status` (enum: NORMAL, ATRASO, FALTA, HORA_EXTRA, ABONO, FERIADO, FERIAS, ATESTADO) — `.../RegistroPonto.java:68-104`
  - `tipoRegistro` (enum: AUTOMATICO, MANUAL, CORRECAO) — `.../RegistroPonto.java:106-110`
  - `falta`, `abono` (boolean, obrigatórios) — `.../RegistroPonto.java:79-84`
- Regras:
  - Jornada de referência: 480 min; calcula atraso ou hora extra — `.../RegistroPonto.java:182-190`
- Exemplos:
  - Válido: data 2025-12-01, entrada1 08:00, saída1 12:00, entrada2 13:00, saída2 17:00 → status NORMAL.
  - Inválido: apenas entrada1 sem saída1 → totais inconsistentes; usar correção.

## Escala de Trabalho
- Campos (origem `src/main/java/com/jaasielsilva/portalceo/model/EscalaTrabalho.java`):
  - `nome`, `descricao`, `tipo` — `.../EscalaTrabalho.java:26-35`
  - Horários e intervalos — `.../EscalaTrabalho.java:36-56`
  - Dias trabalhados flags — `.../EscalaTrabalho.java:58-77`
  - `toleranciaAtraso`, `minutosTolerancia`, vigências — `.../EscalaTrabalho.java:78-92`
- Exemplos:
  - Válido: tipo NORMAL, entrada1 08:00, saída1 12:00, entrada2 13:00, saída2 17:00.

## Folha de Pagamento / Holerite
- Folha (origem `src/main/java/com/jaasielsilva/portalceo/model/FolhaPagamento.java`):
  - `mesReferencia`, `anoReferencia`, totais (`totalBruto`, `totalDescontos`, `totalLiquido`, `totalInss`, `totalIrrf`, `totalFgts`), `status`, datas — `.../FolhaPagamento.java:24-57,68-76`
- Holerite (origem `src/main/java/com/jaasielsilva/portalceo/model/Holerite.java`):
  - Proventos: `salarioBase`, `horasExtras`, `adicionalNoturno`, `periculosidade`, `insalubridade`, `comissoes`, `bonificacoes`, `valeTransporte`, `valeRefeicao`, `auxilioSaude` — `.../Holerite.java:33-63`
  - Descontos: `inss`, `irrf`, `fgts`, `valeTransporte`, `valeRefeicao`, `planoSaude`, `outrosDescontos` — `.../Holerite.java:66-86`
  - Totais: `totalProventos`, `totalDescontos`, `salarioLiquido` — `.../Holerite.java:87-91,126-150`
  - Frequência: `diasTrabalhados`, `horasTrabalhadas`, `faltas`, `atrasos` — `.../Holerite.java:93-104`
- Exemplos:
  - Válido: salarioBase 3500.00, horasExtras 200.00, inss 300.00 → líquido = proventos − descontos.

## Benefícios
- Benefício (origem `src/main/java/com/jaasielsilva/portalceo/model/Beneficio.java`): `nome` obrigatório — `.../Beneficio.java:23-25`.
- Associação (origem `src/main/java/com/jaasielsilva/portalceo/model/ColaboradorBeneficio.java`): `valor` ≥ 0, `status` (ATIVO/INATIVO) — `.../ColaboradorBeneficio.java:31-41`.

## Avaliação de Desempenho
- Campos (origem `src/main/java/com/jaasielsilva/portalceo/model/AvaliacaoDesempenho.java`): período início/fim, `status` (ABERTA, SUBMETIDA, APROVADA, REPROVADA), `nota`, `feedback` — `.../AvaliacaoDesempenho.java:31-58`.
- Exemplos:
  - Válido: ABERTA com período; SUBMETIDA com `dataSubmissao` e `nota`.

## Recrutamento
- Candidato (origem `src/main/java/com/jaasielsilva/portalceo/model/recrutamento/RecrutamentoCandidato.java`): nome, email, telefone, gênero, nascimento com índices — `.../RecrutamentoCandidato.java:26-35`.
- Demais entidades: Vaga, Candidatura, Entrevista, Avaliação, Divulgação — `src/main/java/com/jaasielsilva/portalceo/model/recrutamento/*`.

## Treinamentos
- Curso, Instrutor, Turma, Matrícula, Frequência, Avaliação — services e APIs completas.

## Formatações e Validações Globais
- Datas: `yyyy-MM-dd` onde indicado.
- Decimais: precisão `(10,2)` em remuneração/benefícios.
- E-mails e CPF com validações padrão.

## Dados Sensíveis — Não Armazenar
- Senhas em texto plano, biometria bruta, diagnósticos médicos, dados de cartão.
- Logs e relatórios devem mascarar `cpf`, `email` conforme perfil.

