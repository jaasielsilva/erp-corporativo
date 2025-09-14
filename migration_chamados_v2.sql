-- Script de migração para atualizar a tabela de chamados
-- Adiciona novos campos conforme plano de refatoração

-- Backup da tabela atual (opcional)
-- CREATE TABLE chamados_backup AS SELECT * FROM chamados;

-- Adicionar novos campos à tabela chamados
ALTER TABLE chamados ADD COLUMN data_inicio_atendimento DATETIME NULL;
ALTER TABLE chamados ADD COLUMN data_fechamento DATETIME NULL;
ALTER TABLE chamados ADD COLUMN sla_vencimento DATETIME NULL;
ALTER TABLE chamados ADD COLUMN subcategoria VARCHAR(50) NULL;
ALTER TABLE chamados ADD COLUMN tempo_resolucao_minutos INT NULL;
ALTER TABLE chamados ADD COLUMN avaliacao INT NULL;
ALTER TABLE chamados ADD COLUMN comentario_avaliacao TEXT NULL;

-- Atualizar SLA de vencimento para chamados existentes
UPDATE chamados 
SET sla_vencimento = CASE 
    WHEN prioridade = 'URGENTE' THEN DATE_ADD(data_abertura, INTERVAL 8 HOUR)
    WHEN prioridade = 'ALTA' THEN DATE_ADD(data_abertura, INTERVAL 24 HOUR)
    WHEN prioridade = 'MEDIA' THEN DATE_ADD(data_abertura, INTERVAL 48 HOUR)
    WHEN prioridade = 'BAIXA' THEN DATE_ADD(data_abertura, INTERVAL 72 HOUR)
    ELSE DATE_ADD(data_abertura, INTERVAL 48 HOUR)
END
WHERE sla_vencimento IS NULL;

-- Atualizar data_inicio_atendimento para chamados em andamento
UPDATE chamados 
SET data_inicio_atendimento = data_abertura
WHERE status = 'EM_ANDAMENTO' AND data_inicio_atendimento IS NULL;

-- Atualizar data_fechamento para chamados fechados
UPDATE chamados 
SET data_fechamento = data_resolucao
WHERE status = 'FECHADO' AND data_fechamento IS NULL AND data_resolucao IS NOT NULL;

-- Calcular tempo de resolução para chamados já resolvidos
UPDATE chamados 
SET tempo_resolucao_minutos = TIMESTAMPDIFF(MINUTE, 
    COALESCE(data_inicio_atendimento, data_abertura), 
    data_resolucao
)
WHERE status IN ('RESOLVIDO', 'FECHADO') 
  AND data_resolucao IS NOT NULL 
  AND tempo_resolucao_minutos IS NULL;

-- Criar índices para melhorar performance
CREATE INDEX idx_chamados_sla_vencimento ON chamados(sla_vencimento);
CREATE INDEX idx_chamados_data_inicio_atendimento ON chamados(data_inicio_atendimento);
CREATE INDEX idx_chamados_data_fechamento ON chamados(data_fechamento);
CREATE INDEX idx_chamados_subcategoria ON chamados(subcategoria);
CREATE INDEX idx_chamados_avaliacao ON chamados(avaliacao);

-- Verificar dados após migração
SELECT 
    'Total de chamados' as descricao,
    COUNT(*) as quantidade
FROM chamados
UNION ALL
SELECT 
    'Chamados com SLA definido' as descricao,
    COUNT(*) as quantidade
FROM chamados 
WHERE sla_vencimento IS NOT NULL
UNION ALL
SELECT 
    'Chamados com tempo de resolução calculado' as descricao,
    COUNT(*) as quantidade
FROM chamados 
WHERE tempo_resolucao_minutos IS NOT NULL
UNION ALL
SELECT 
    'Chamados avaliados' as descricao,
    COUNT(*) as quantidade
FROM chamados 
WHERE avaliacao IS NOT NULL;

-- Comentários sobre a migração
/*
Esta migração adiciona os seguintes campos:

1. data_inicio_atendimento: Quando o técnico iniciou o atendimento
2. data_fechamento: Quando o chamado foi definitivamente fechado
3. sla_vencimento: Data/hora limite para resolução baseada na prioridade
4. subcategoria: Classificação mais específica do problema
5. tempo_resolucao_minutos: Tempo total gasto na resolução
6. avaliacao: Nota de 1-5 estrelas dada pelo usuário
7. comentario_avaliacao: Feedback textual do usuário

Os índices criados melhoram a performance de consultas por:
- SLA vencido ou próximo do vencimento
- Relatórios de tempo de atendimento
- Análise de avaliações
- Filtros por subcategoria

Após executar esta migração:
1. Reinicie a aplicação
2. Teste a criação de novos chamados
3. Verifique se os cálculos de SLA estão corretos
4. Confirme que as avaliações funcionam
*/