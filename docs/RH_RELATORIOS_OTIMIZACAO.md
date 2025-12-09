# Otimização de Desempenho — Módulo RH/Relatórios

## 1. Avaliação de desempenho
- Tempo de resposta atual: endpoints com agregações mensais, com melhora após refatoração das consultas
- Consultas mais lentas: contagens mês a mês de admissões e desligamentos e cálculo de séries
- Índices verificados: poucos índices em colunas de data e relacionamento; adição proposta abaixo

## 2. Propostas de otimização
- Views e tabelas materializadas
  - Views: `vw_admissoes_mes`, `vw_desligamentos_mes`
  - Materializadas: `mv_admissoes_mes`, `mv_desligamentos_mes` com `sp_refresh_mv_rh_relatorios`
- Cache de resultados
  - Cache com TTL de 15 min no relatório de Turnover detalhado
- Consultas otimizadas
  - Agregações SQL com `GROUP BY YEAR/MONTH` e filtros opcionais
  - Índices: datas e chaves de relacionamento
  - Particionamento sugerido para alto volume

## 3. Implementação
- Scripts em `src/main/resources/db/migration/V20251208__rh_relatorios_views.sql`
  - Índices e criação de views/tabelas materializadas
- Código atualizado
  - Repositórios: métodos `contarAdmissoesPorMes`, `contarDesligamentosPorMes`
  - Service: uso dessas agregações em `gerarRelatorioTurnoverDetalhado` e cache com TTL
- Compatibilidade
  - Fallback natural: sem dependência obrigatória das views; usa consultas agregadas nativas

## 4. Testes
- Comparação de desempenho
  - Antes: múltiplas leituras e filtragem em memória
  - Depois: 2 consultas agregadas por período + cache
- Consistência dos dados
  - Comparar totais entre versão antiga e nova com período controlado
- Volumes altos
  - Popular tabelas com 1M registros e avaliar `EXPLAIN` nas queries

## 5. Documentação
- Boas práticas
  - Índices em colunas de data (`data_admissao`, `data_registro`)
  - Evitar `LIKE` sem índices; usar `FULLTEXT` onde aplicável
  - Preferir agregações SQL ao processamento em memória
  - Cache em consultas exploratórias repetitivas
- Estrutura das views
  - `vw_admissoes_mes(ano,mes,qtd)`
  - `vw_desligamentos_mes(ano,mes,qtd)`
- Atualização das materializadas
  - Rodar `CALL sp_refresh_mv_rh_relatorios();` diariamente ou após grandes cargas
