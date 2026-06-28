USE clickfarma;

INSERT INTO categorias (nome) VALUES 
('Analgésicos'),
('Antibióticos'),
('Anti-inflamatórios'),
('Vitaminas'),
('Higiene Pessoal')
ON DUPLICATE KEY UPDATE nome=nome;

INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Dipirona 500mg', 'Analgésico para dores e febre', 8.50, 1, 100),
('Paracetamol 750mg', 'Analgésico e antitérmico', 12.90, 1, 150),
('Ibuprofeno 400mg', 'Anti-inflamatório não esteroidal', 15.50, 3, 80),
('Amoxicilina 500mg', 'Antibiótico para infecções bacterianas', 25.90, 2, 60),
('Vitamina C 1g', 'Suplemento vitamínico para imunidade', 35.00, 4, 200),
('Sabonete Líquido', 'Higiene pessoal para mãos', 9.90, 5, 300);

INSERT INTO usuarios (nome, email, senha, endereco, role, data_cadastro) VALUES
('Gustavson', 'gustavson.adm@gmail.com', '$2a$10$8K1p/a0dL3LzY5T4m7x0OeLzQ5bM9nJ8vU5sR2tW3yX4zC5vB6nO', 'Endereço Teste', 'ADMIN', NOW());

INSERT IGNORE INTO entregadores (nome, cpf, senha, telefone, cnh, placa_veiculo, modelo_veiculo, chave_pix, status, ativo, data_cadastro)
VALUES ('Entregador Teste', '12345678901', '$2a$10$5MP/RkYWSU9Asy1KUPDYkOLk0fljksOQLMf9gO4UrOOs3SMuApCcy', '(81) 99999-0000', '12345678901', 'ABC-1234', 'Honda CG 160', '12345678901', 'ATIVO', true, NOW());
