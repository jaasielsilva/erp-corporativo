-- Script para verificar dados após teste do sistema de suporte
USE painelceo;

-- 1. Verificar se o chamado foi criado
SELECT 
    id,
    numero,
    assunto,
    categoria,
    subcategoria,
    prioridade,
    status,
    solicitante_nome,
    solicitante_email,
    descricao,
    data_criacao
FROM chamados 
ORDER BY data_criacao DESC 
LIMIT 5;

-- 2. Contar total de chamados
SELECT COUNT(*) as total_chamados FROM chamados;

-- 3. Verificar distribuição por status
SELECT status, COUNT(*) as quantidade 
FROM chamados 
GROUP BY status;

-- 4. Verificar distribuição por prioridade
SELECT prioridade, COUNT(*) as quantidade 
FROM chamados 
GROUP BY prioridade;

-- 5. Verificar se subcategoria foi salva
SELECT categoria, subcategoria, COUNT(*) as quantidade
FROM chamados 
WHERE subcategoria IS NOT NULL
GROUP BY categoria, subcategoria;