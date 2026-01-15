-- SQL Script to create Juridico Users and Profiles
USE painelceo;

-- 1. Create Profiles if they don't exist
INSERT IGNORE INTO perfis (nome) VALUES ('GERENTE_JURIDICO');
INSERT IGNORE INTO perfis (nome) VALUES ('ESTAGIARIO_JURIDICO');

-- Get Profile IDs
SET @gerente_id = (SELECT id FROM perfis WHERE nome = 'GERENTE_JURIDICO');
SET @estagiario_id = (SELECT id FROM perfis WHERE nome = 'ESTAGIARIO_JURIDICO');

-- 2. Link Permissions to GERENTE_JURIDICO (Full Access)
DELETE FROM perfil_permissao WHERE perfil_id = @gerente_id;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT @gerente_id, id FROM permissoes 
WHERE nome IN (
    'ROLE_USER', 'MENU_CLIENTES', 'MENU_CLIENTES_LISTAR', 'MENU_CLIENTES_NOVO', 
    'MENU_CLIENTES_CONTRATOS_LISTAR', 'MENU_CLIENTES_HISTORICO_INTERACOES', 
    'MENU_CLIENTES_HISTORICO_PEDIDOS', 'MENU_CLIENTES_AVANCADO_BUSCA', 
    'MENU_CLIENTES_AVANCADO_RELATORIOS', 'MENU_JURIDICO', 'MENU_JURIDICO_DASHBOARD', 
    'MENU_JURIDICO_CLIENTES', 'MENU_JURIDICO_PREVIDENCIARIO', 
    'MENU_JURIDICO_PREVIDENCIARIO_LISTAR', 'MENU_JURIDICO_PREVIDENCIARIO_NOVO', 
    'MENU_JURIDICO_WHATCHAT', 'MENU_JURIDICO_CONTRATOS', 'MENU_JURIDICO_PROCESSOS', 
    'MENU_JURIDICO_PROCESSOS_LISTAR', 'MENU_JURIDICO_PROCESSOS_NOVO', 
    'MENU_JURIDICO_COMPLIANCE', 'MENU_JURIDICO_DOCUMENTOS', 
    'MENU_JURIDICO_AUDITORIA', 'MENU_JURIDICO_AUDITORIA_INICIO', 
    'MENU_JURIDICO_AUDITORIA_ACESSOS', 'MENU_JURIDICO_AUDITORIA_ALTERACOES', 
    'MENU_JURIDICO_AUDITORIA_EXPORTACOES', 'MENU_JURIDICO_AUDITORIA_REVISOES',
    'DASHBOARD_EXECUTIVO_VISUALIZAR'
);

-- 3. Link Permissions to ESTAGIARIO_JURIDICO (Limited Access)
DELETE FROM perfil_permissao WHERE perfil_id = @estagiario_id;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT @estagiario_id, id FROM permissoes 
WHERE nome IN (
    'ROLE_USER', 'MENU_CLIENTES', 'MENU_CLIENTES_LISTAR', 
    'MENU_JURIDICO', 'MENU_JURIDICO_CLIENTES', 
    'MENU_JURIDICO_PREVIDENCIARIO', 'MENU_JURIDICO_PREVIDENCIARIO_LISTAR', 
    'MENU_JURIDICO_CONTRATOS', 'MENU_JURIDICO_PROCESSOS', 
    'MENU_JURIDICO_PROCESSOS_LISTAR', 'MENU_JURIDICO_DOCUMENTOS'
);

-- 4. Create Users (Password: 123456)
SET @pwd = '$2a$10$ajl2yELisfQ.IjrME36OaOzLCTEPjp8QrH3CKO7Gt3MWGUxb3Xa5q';

-- Create Gerente Juridico (Dept 7 is Juridico based on usual ID)
-- Let's check department ID first to be safe, but typically 7.
INSERT IGNORE INTO usuarios (nome, email, senha, matricula, cpf, nivel_acesso, status, departamento_id)
VALUES ('Gerente Jurídico', 'gerente.juridico@portalceo.com', @pwd, 'JUR001', '00011122233', 'GERENTE', 'ATIVO', (SELECT id FROM departamentos WHERE nome LIKE '%Jurídico%' LIMIT 1));

SET @user_gerente_id = (SELECT id FROM usuarios WHERE email = 'gerente.juridico@portalceo.com');
INSERT IGNORE INTO usuario_perfil (usuario_id, perfil_id) VALUES (@user_gerente_id, @gerente_id);

-- Create Estagiario Juridico
INSERT IGNORE INTO usuarios (nome, email, senha, matricula, cpf, nivel_acesso, status, departamento_id)
VALUES ('Estagiário Jurídico', 'estagiario.juridico@portalceo.com', @pwd, 'JUR002', '11122233344', 'ESTAGIARIO', 'ATIVO', (SELECT id FROM departamentos WHERE nome LIKE '%Jurídico%' LIMIT 1));

SET @user_estagiario_id = (SELECT id FROM usuarios WHERE email = 'estagiario.juridico@portalceo.com');
INSERT IGNORE INTO usuario_perfil (usuario_id, perfil_id) VALUES (@user_estagiario_id, @estagiario_id);
