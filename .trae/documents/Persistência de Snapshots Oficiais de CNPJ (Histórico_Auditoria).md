## Objetivo

Criar a tabela `cnpj_consultas` para armazenar snapshots oficiais consultados (razão social, fantasia, endereço, situação, CNAE principal e secundários), integrando consulta individual e processamento em lote, mantendo o cache para performance.

## Estrutura de Dados

* Entidade `CnpjConsulta` (`cnpj_consultas`):

  * `cnpj`, `razaoSocial`, `nomeFantasia`, `situacaoCadastral`

  * Endereço: `logradouro`, `numero`, `complemento`, `bairro`, `municipio`, `uf`, `cep`

  * `cnaePrincipalCodigo`, `cnaePrincipalDescricao`

  * `consultedAt`, `source`, `protocol` (opcional)

  * `@ElementCollection` `cnaesSecundarios` (tabela `cnpj_consultas_cnaes_sec`) com `codigo`, `descricao`

* Índice por `cnpj` para consultas rápidas.

## Integração

* `ReceitaService.consultarCnpj(...)` salva snapshot após obter o DTO do provider (apenas em miss de cache; hits permanecem no cache e podem já ter snapshot de uma consulta anterior).

* Service `CnpjConsultaLogService` mapeia DTO → entidade e persiste com `source` (`erp.receita.base-url`).

## Impactos

* Sem alteração de regra de negócio: cache continua evitando chamadas; snapshots se acumulam para auditoria/histórico.

* Sem migrações obrigatórias: JPA `update` cria as tabelas.

## Verificação

* Consultas individuais e lote persistem o snapshot na primeira vez (miss de cache); hits usam cache e mantêm histórico anterior.

## Próximos passos criar os endpoint e paginas

* Endpoint para listar histórico por CNPJ.

* Política de retenção: expirar snapshots antigos conforme necessidade.

