-- =====================================================
-- Tabela para Remember-Me Tokens Persistentes
-- ERP Corporativo - Melhorias de Segurança
-- =====================================================

-- Criar tabela para tokens persistentes do Remember-Me
CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Índices para performance
    INDEX idx_username (username),
    INDEX idx_last_used (last_used)
);

-- Comentários para documentação
ALTER TABLE persistent_logins 
COMMENT = 'Tabela para armazenar tokens persistentes do Remember-Me';

ALTER TABLE persistent_logins 
MODIFY COLUMN username VARCHAR(64) NOT NULL 
COMMENT 'Email/username do usuário';

ALTER TABLE persistent_logins 
MODIFY COLUMN series VARCHAR(64) NOT NULL 
COMMENT 'Série única do token';

ALTER TABLE persistent_logins 
MODIFY COLUMN token VARCHAR(64) NOT NULL 
COMMENT 'Token hash para autenticação';

ALTER TABLE persistent_logins 
MODIFY COLUMN last_used TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP 
COMMENT 'Último uso do token';

-- Procedure para limpeza automática de tokens expirados (opcional)
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS CleanExpiredTokens()
BEGIN
    -- Remove tokens com mais de 30 dias
    DELETE FROM persistent_logins 
    WHERE last_used < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- Log da limpeza
    SELECT CONCAT('Tokens expirados removidos em: ', NOW()) as cleanup_log;
END //

DELIMITER ;

-- Event para executar limpeza automática diariamente (opcional)
-- Descomente se quiser limpeza automática
/*
CREATE EVENT IF NOT EXISTS cleanup_expired_tokens
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
  CALL CleanExpiredTokens();
*/