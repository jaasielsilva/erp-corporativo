## Visão Geral

* Implementar integração de consulta de CNPJ com cache de 24h, página de consulta, processamento em lote assíncrono e itens na sidebar.

* Usar Spring Boot 3, WebClient, Spring Cache (Caffeine), Spring Async, JPA, Thymeleaf, Bootstrap 5 e ES6.

* Provider de API pública (BrasilAPI por padrão) com abstração para fácil troca.

## Premissas

* Java 17+, Spring Boot 3 já em uso no ERP.

* Usaremos Caffeine para TTL configurável (fallback possível para ConcurrentMapCache).

* Fonte de CNPJs para o lote: repository paginado retornando `List<String>` (pode ser adaptado às suas entidades).

* Validação/sanitização básica do CNPJ (remoção de máscara, checagem de tamanho/zeros).

## Arquitetura e Pacotes

* `br.com.seu.erp.cnpj` (módulo)

  * `config` → Cache, Async, WebClient, propriedades

  * `controller` → `ConsultaCnpjController`, `ProcessamentoCnpjController`

  * `service` → `ReceitaService`, `ProcessamentoCnpjService`

  * `provider` → `CnpjProvider` (interface) e `BrasilApiCnpjProvider` (impl)

  * `dto` → `CnpjConsultaDto`, `EnderecoDto`, `CnaeDto`

  * `repository` → `CnpjFonteRepository` (interface), `CnpjFonteRepositoryImpl` (impl JPA paginada)

  * `util` → `CnpjUtils`

  * `web` → páginas Thymeleaf, JS e fragmento de sidebar

## Configuração

* `@EnableCaching` e `@EnableAsync` com `ThreadPoolTaskExecutor` (tamanho ajustável).

* `CacheConfig` (Caffeine): nome `cnpjCache`, TTL padrão 24h, `maximumSize` configurável.

* `WebClientConfig`: timeouts, baseURL configurável via propriedades, `ExchangeFilterFunction` para log básico e tratamento de erros (HTTP 4xx/5xx, 429).

* `application.yml`:

  * `erp.receita.base-url: https://brasilapi.com.br/api/cnpj/v1`

  * `erp.receita.timeout-ms: 8000`

  * `erp.cnpj-cache.ttl-hours: 24`

  * `erp.cnpj-cache.max-size: 100000`

## DTOs

* `CnpjConsultaDto`: `razaoSocial`, `nomeFantasia`, `endereco` (`EnderecoDto`), `situacaoCadastral`, `cnaePrincipal` (`CnaeDto`), `cnaesSecundarios` (`List<CnaeDto>`)

* `EnderecoDto`: `logradouro`, `numero`, `complemento`, `bairro`, `municipio`, `uf`, `cep`

* `CnaeDto`: `codigo`, `descricao`

## Provider de API

* `CnpjProvider` com método `CnpjConsultaDto consultar(String cnpj)`.

* `BrasilApiCnpjProvider` mapeia resposta da BrasilAPI para os DTOs.

* Facilidade para trocar para outra API pública apenas criando nova impl e configurando.

## Services

* `ReceitaService`

  * `@Cacheable(value = "cnpjCache", key = "#cnpj")`

  * `consultarCnpj(String cnpj)`: sanitiza, valida e consulta via `CnpjProvider` usando `WebClient`.

* `ProcessamentoCnpjService`

  * `@Async` `processarAsync()`: pagina com `LIMIT 200 OFFSET X`, chama `ReceitaService` para cada CNPJ. Usa cache automaticamente.

  * Registra início/fim em log; expõe status simples em memória para polling.

## Repository (paginação 200/offset)

* `CnpjFonteRepository`

  * `List<String> buscarLote(int limit, int offset)`

* `CnpjFonteRepositoryImpl` (JPA)

  * Native query configurável via propriedade `erp.cnpj-fonte.query`: ex. `SELECT cnpj FROM empresas ORDER BY id LIMIT :limit OFFSET :offset`.

  * Retorna somente CNPJs válidos/sanitizados.

## Controllers

* `ConsultaCnpjController`

  * GET `/cadastros/consultar-cnpj`: retorna página Thymeleaf.

  * GET `/cadastros/consultar?cnpj=xxxxx`: retorna JSON `CnpjConsultaDto`.

* `ProcessamentoCnpjController`

  * GET `/utilidades/processar-cnpj`: página com botão.

  * POST `/utilidades/processar`: dispara `processarAsync()` e retorna 202.

  * GET `/utilidades/processamento-status`: retorna status simples para polling.

## Páginas Thymeleaf

* `templates/cadastros/consultar-cnpj.html`

  * Campo CNPJ, botão “Consultar Receita”, área de resultado; Bootstrap Toast para avisos.

* `templates/utilidades/processar-cnpj.html`

  * Botão “Processar CNPJs”, Toast “Processamento iniciado”, exemplo de polling para Toast final.

* `templates/components/sidebar.html`

  * Itens de menu: Gestão → Cadastros → Consultar CNPJ; Gestão → Utilidades → Processar CNPJs.

## JavaScript (ES6)

* `static/js/cnpj-consulta.js`

  * Captura input, chama `/cadastros/consultar`, mostra toasts, popula resultado.

* `static/js/cnpj-processar.js`

  * POST `/utilidades/processar`, Toast inicial, polling `GET /utilidades/processamento-status` para Toast final.

## Cache

* Nome: `cnpjCache`.

* TTL: 24h (configurável).

* Operação: `@Cacheable` no service; hits evitam chamadas externas no online e no lote.

## Tratamento de Erros

* Validação CNPJ (formato/tamanho); sanitização.

* Mapeamento de respostas de erro da API (404/429/5xx) com mensagens amigáveis.

* Retorno HTTP adequado (400 para input inválido, 502 para falha externa, 429 para rate limit detectado).

## Segurança e Perfomance

* Timeouts e retry simples (opcional, com backoff leve quando 429).

* Thread pool assíncrono dedicado; limitar concorrência se necessário.

* Tamanho do cache controlado; evitar dados sensíveis.

## Verificação

* Testes unitários dos serviços (mock do provider).

* Teste do cache (hit/miss, TTL).

* Teste da paginação do repository (limit/offset).

* Smoke test de páginas Thymeleaf com WebClient real em ambiente dev.

## Propriedades e Ajustes

* `erp.receita.base-url`, `erp.receita.timeout-ms`, `erp.cnpj-cache.ttl-hours`, `erp.cnpj-cache.max-size`, `erp.cnpj-fonte.query`.

## Entregáveis

* Controllers, Services, Config de Cache/Async/WebClient, Repository paginado, Páginas Thymeleaf, JavaScript, DTOs, Provider de API, fragmento de sidebar, propriedades.

## Depois da Aprovação

* Implementação completa dos arquivos, testes, e validação em runtime; ajuste fino para sua tabela/consulta de CNPJs.

