# Módulo de Recrutamento – Manual e Casos de Uso

## Visão Geral
- Objetivo: gerir candidatos, vagas, candidaturas, entrevistas e avaliações.
- Páginas principais:
  - `rh/recrutamento/candidatos`: listagem, filtros, criação de candidatos, candidatura.
  - `rh/recrutamento/vagas`: criação de vagas com seleção de cargo e departamento.
- Perfis de acesso: `ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER`, `ROLE_GERENCIAL`.

## Rotas de Página
- `GET /rh/recrutamento/candidatos` abre `src/main/resources/templates/rh/recrutamento/candidatos.html` via `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoController.java:53`.
- `GET /rh/recrutamento/vagas` abre `src/main/resources/templates/rh/recrutamento/vagas.html` via `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoController.java:61`.

## APIs
- Listar candidatos (paginado + filtros + cache curto)
  - `GET /api/rh/recrutamento/candidatos`
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:148`
  - Parâmetros: `q, nome, email, telefone, genero, nasc, page, size`
  - Retorna `Page<RecrutamentoCandidato>` com `Cache-Control: private, max-age=30`.
- Criar candidato
  - `POST /api/rh/recrutamento/candidatos`
  - `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:32`
  - Parâmetros: `nome` (obrigatório), `email` válido, `telefone`, `genero`, `dataNascimento`.
- Listar vagas
  - `GET /api/rh/recrutamento/vagas`
  - `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:99`
  - Parâmetros: `status` opcional, `page`, `size`.
- Prefill de cargos com departamentos associados
  - `GET /api/rh/recrutamento/cargos`
  - `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:107`
- Listar todos os departamentos
  - `GET /api/rh/recrutamento/departamentos`
  - `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:142`
- Criar vaga
  - `POST /api/rh/recrutamento/vagas`
  - `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:82`
- Candidatar candidato a vaga
  - `POST /api/rh/recrutamento/candidaturas`
  - `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:165`
- Entrevistas
  - `POST /api/rh/recrutamento/candidaturas/{id}/entrevistas`
  - `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:185`
- Avaliações
  - `POST /api/rh/recrutamento/candidaturas/{id}/avaliacao`
  - `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:196`

## Casos de Uso
- Criar candidato
  - Acessar `Recrutamento - Candidatos`, preencher `Nome` e `Email`, acionar `Criar`.
  - Backend valida `nome` e `email`; em caso de sucesso recarrega listagem.
- Listar e filtrar candidatos
  - Usar busca livre (`q`) e filtros específicos: `Nome`, `Email`, `Telefone`, `Gênero`, `Nascimento`.
  - Navegar por páginas e alterar `Itens por página`; estado de filtros é preservado no cliente.
- Candidatar candidato a vaga
  - Selecionar uma `vaga` por linha e pressionar `Candidatar`.
  - Cria `RecrutamentoCandidatura` com etapa inicial `TRIAGEM`.
- Criar vaga com seleção de departamento
  - Selecionar `Cargo`, revisar campos sugeridos, clicar no campo `Departamento`.
  - Caso não existam associações: marcar `Mostrar todos os departamentos` e selecionar manualmente.
  - Validar associação: se houver lista associada, bloqueia departamentos não associados.
- Agendar entrevista
  - Selecionar candidatura e enviar `inicio`, `fim`, `local`, `tipo`.
  - Gera evento na agenda e e-mail para o candidato (se presente).
- Avaliar candidato
  - Enviar `nota` e `feedback` para a candidatura.

## Validações e Regras
- Criar candidato: `nome` obrigatório, `email` válido (`RecrutamentoApiController.java:39`).
- Criar vaga: valida departamento conforme associações de cargo (`vagas.html:33`).
- Candidatura duplicada é impedida (`RecrutamentoService.java:79`).

## Modelo de Dados
- `RecrutamentoCandidato` com índices em `nome`, `email`, `telefone`, `genero`, `dataNascimento`:
  - `src/main/java/com/jaasielsilva/portalceo/model/recrutamento/RecrutamentoCandidato.java:12`

## Desempenho
- Cache curto em memória (Caffeine) para páginas/filtros:
  - `src/main/java/com/jaasielsilva/portalceo/service/rh/recrutamento/RecrutamentoService.java:136` (`@Cacheable`).
  - Cache Manager com política específica: `src/main/java/com/jaasielsilva/portalceo/config/CacheConfig.java:23`.
- Cache HTTP na resposta:
  - `RecrutamentoApiController.java:148` define `Cache-Control`.
- Compressão Gzip:
  - `src/main/resources/application.properties:93`.
- Consulta read-only JPA:
  - `src/main/java/com/jaasielsilva/portalceo/repository/recrutamento/RecrutamentoCandidatoRepository.java:13`.
- Frontend com debounce e scroll infinito:
  - `src/main/resources/templates/rh/recrutamento/candidatos.html:40`.

## Perfis e Segurança
- Autorização por perfil nos endpoints e páginas:
  - `RecrutamentoApiController.java:148`, `RecrutamentoController.java:53`/`61`.

## Monitoramento
- Actuator/Micrometer Prometheus habilitado (`application.properties:88`).
- Recomendado: expor métricas customizadas de latência e acertos de cache no serviço de candidatos.

## Critérios de Sucesso
- Tempo de carregamento abaixo de 2s para 90% dos usuários em listas paginadas.
- Consumo de memória reduzido em ≥40% com cache curto e compressão.
- Continuidade da funcionalidade em picos (cache HTTP e Caffeine diminuem pressão em DB).

## Troubleshooting
- Página `candidatos` travada:
  - Verificar rede para `GET /api/rh/recrutamento/candidatos` e `GET /api/rh/recrutamento/vagas`.
  - Confirmar debounce e `isLoading` ativo; checar se o sentinela (`#listSentinel`) está presente.
- Lista de departamentos vazia:
  - Conferir `GET /api/rh/recrutamento/departamentos` e permissões.
- Candidatura não cria:
  - Checar erro de duplicidade (`RecrutamentoService.java:79`).

## Referências Rápidas
- Página Candidatos: `src/main/resources/templates/rh/recrutamento/candidatos.html:1`
- Página Vagas: `src/main/resources/templates/rh/recrutamento/vagas.html:1`
- Controller página: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoController.java:53,61`
- API candidatos: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java:148`
- Serviço candidatos: `src/main/java/com/jaasielsilva/portalceo/service/rh/recrutamento/RecrutamentoService.java:136`
- Repositório query: `src/main/java/com/jaasielsilva/portalceo/repository/recrutamento/RecrutamentoCandidatoRepository.java:13`
