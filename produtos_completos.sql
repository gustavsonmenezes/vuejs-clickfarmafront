USE clickfarma;

-- Inserir categorias adicionais
INSERT INTO categorias (nome) VALUES 
('Diabetes'),
('Pressão Arterial'),
('Colesterol'),
('Alergia'),
('Digestivo'),
('Pele'),
('Respiratório'),
('Vitamias e Suplementos'),
('Infantil')
ON DUPLICATE KEY UPDATE nome=nome;

-- Diabetes (categoria_id 6)
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Glicometro Accu-Chek', 'Medidor de glicose sanguinea digital', 189.90, 6, 50),
('Tiras Teste Glicose Accu-Chek 50un', 'Tiras reagentes para glicometro', 89.90, 6, 100),
('Metformina 500mg', 'Medicamento para controle de diabetes tipo 2', 15.90, 6, 120),
('Insulina NPH 100UI/mL', 'Insulina intermediaria para diabetes', 45.00, 6, 40),
('Seringa de Insulina 1ml', 'Seringa descartavel para aplicacao de insulina', 12.50, 6, 200);

-- Pressão Arterial (categoria_id 7)
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Losartana 50mg', 'Medicamento para hipertensao arterial', 18.50, 7, 150),
('Enalapril 10mg', 'Inibidor da ECA para pressao alta', 22.90, 7, 100),
('Captopril 25mg', 'Anti-hipertensivo para crise pressorica', 8.90, 7, 180),
('Anlodipino 5mg', 'Calcio antagonista para hipertensao', 25.50, 7, 90);

-- Colesterol (categoria_id 8)
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Sinvastatina 20mg', 'Estatina para reducao de colesterol', 28.90, 8, 80),
('Atorvastatina 40mg', 'Estatina potente para colesterol alto', 35.50, 8, 70),
('Omega 3 1000mg', 'Suplemento para saude cardiovascular', 42.00, 8, 150);

-- Alergia (categoria_id 9)
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Loratadina 10mg', 'Antialergico sem causa sono', 12.50, 9, 200),
('Cetirizina 10mg', 'Antialergico para rinite e urticaria', 15.90, 9, 180),
('Desloratadina 5mg', 'Antialergico 24h non-drowsy', 28.50, 9, 100),
('Pomada Bepantol', 'Pomada para assaduras e alergias na pele', 32.90, 9, 90);

-- Digestivo (categoria_id 10)
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Omeprazol 20mg', 'Protetor gastrico para gastrite e refluxo', 18.90, 10, 150),
('Buscopan 10mg', 'Antiespasmodico para colicas intestinais', 22.50, 10, 120),
('Butilescopolamina 10mg', 'Relaxante muscular para colicas', 28.90, 10, 100),
('Metoclopramida 10mg', 'Anti-nauseico e procinetico gastrico', 15.50, 10, 130);

-- Pele (categoria_id 11)
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Protetor Solar FPS 60', 'Protetor solar alta protecao', 39.90, 11, 100),
('Hidratante Corporal', 'Creme hidratante para pele seca', 29.90, 11, 120),
('Pomada Aciclovir', 'Antiviral para herpes labial', 18.50, 11, 80),
('Bepantol Pomada', 'Cicatrizante e regenerador cutaneo', 35.50, 11, 90);

-- Respiratório (categoria_id 12)
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Dipirona 1g', 'Analgésico e antitérmico forte', 12.90, 12, 150),
('Xarope Vick', 'Xarope para tosse e congestao nasal', 22.90, 12, 80),
('Salbutamol Spray', 'Broncodilatador para asma', 28.50, 12, 60),
('Lavanda Essencial', 'Oleo essencial para inalacao e relaxamento', 24.90, 12, 70);

-- Vitaminas (categoria_id 4)
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Vitamina D3 5000UI', 'Suplemento para imunidade e ossos', 45.00, 4, 100),
('Complexo B', 'Vitaminas do complexo B para energia', 28.50, 4, 120),
('Magnesio Quelato', 'Suplemento para relaxamento muscular', 38.90, 4, 90),
('Zinco 50mg', 'Mineral para imunidade e pele', 32.50, 4, 110);

SELECT COUNT(*) as total_produtos FROM produtos;
