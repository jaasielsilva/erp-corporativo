CREATE OR REPLACE VIEW view_ticket_medio AS
SELECT 
    SUM(total) / COUNT(*) AS ticket_medio
FROM venda;
-- Criação da view para calcular o ticket médio

CREATE OR REPLACE VIEW view_ticket_medio AS
SELECT 
    ROUND(SUM(total) / COUNT(*), 2) AS ticket_medio
FROM venda
WHERE status = 'FINALIZADA';
