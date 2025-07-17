-- CATEGORIAS
INSERT INTO categoria (id, nome) VALUES
(1, 'Papelaria'),
(2, 'Mochilas'),
(3, 'Escritório');

-- FORNECEDORES
INSERT INTO fornecedor (id, nome, cnpj, email, telefone) VALUES
(1, 'Fornecedor Alpha', '12345678000101', 'contato@alpha.com', '(11) 99999-0001'),
(2, 'Fornecedor Beta', '12345678000102', 'contato@beta.com', '(11) 99999-0002'),
(3, 'Fornecedor Gama', '12345678000103', 'contato@gama.com', '(11) 99999-0003');

-- PRODUTOS
INSERT INTO produto (id, nome, ean, preco, estoque, unidade_medida, ativo, data_cadastro, categoria_id, fornecedor_id) VALUES
(1, 'Caneta Azul', '7891234567890', 2.50, 100, 'UN', true, NOW(), 1, 1),
(2, 'Caderno 100 folhas', '7891234567891', 12.90, 50, 'UN', true, NOW(), 1, 1),
(3, 'Mochila Escolar', '7891234567892', 89.90, 20, 'UN', true, NOW(), 2, 2),
(4, 'Papel Sulfite A4', '7891234567893', 28.00, 60, 'PCT', true, NOW(), 3, 3),
(5, 'Grampeador', '7891234567894', 25.00, 40, 'UN', true, NOW(), 3, 2);

-- CLIENTE
INSERT INTO cliente (
    id, nome, cpf_cnpj, tipo_cliente, email, telefone, celular, cep, logradouro, numero, bairro,
    cidade, estado, complemento, ativo, status, data_cadastro, data_criacao
) VALUES (
    1, 'Cliente Teste', '12345678900', 'PF', 'cliente@teste.com', '(11) 3333-0000', '(11) 98888-0000',
    '01001-000', 'Rua de Exemplo', '100', 'Centro', 'São Paulo', 'SP', '', true, 'Ativo',
    CURDATE(), NOW()
);
