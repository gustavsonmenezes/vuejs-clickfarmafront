USE clickfarma;

-- Adicionar produtos que a IA esta sugerindo mas nao existem
INSERT INTO produtos (nome, descricao, preco, categoria_id, estoque) VALUES
('Glibenclamida 5mg', 'Medicamento para diabetes tipo 2', 19.90, 6, 80),
('Fenistil 1% Gel', 'Gel para alergias e picadas de insetos', 28.50, 9, 90),
('Gliclazida 30mg', 'Antidiabético oral', 22.90, 6, 70),
('Vildagliptina 50mg', 'Inibidor DPP-4 para diabetes', 38.50, 6, 60),
('Aspirina 500mg', 'Analgésico e anticoagulante', 9.90, 1, 200),
('Dipirona 1g', 'Analgésico e antitérmico forte', 12.90, 1, 150),
('Rivotril 2mg', 'Ansiolitico para crises de ansiedade', 45.50, 1, 40),
('Fluconazol 150mg', 'Antifúngico para candidiase', 18.90, 2, 100),
('Azitromicina 500mg', 'Antibiótico para infecções respiratórias', 32.50, 2, 80),
('Lorazepam 2mg', 'Sedativo e ansiolitico', 38.90, 1, 50),
('Tylenol 750mg', 'Analgésico e antitérmico', 14.90, 1, 120),
('Neosaldina', 'Analgésico para enxaqueca', 16.50, 1, 90),
('Benegrippe', 'Medicamento para sintomas de gripe', 19.90, 12, 100),
('Respirin', 'Xarope para tosse seca', 24.90, 12, 80),
('Pantoprazol 40mg', 'Protetor gastrico', 21.50, 10, 110),
('Citalopram 20mg', 'Antidepressivo ISRS', 28.90, 1, 70),
('Escitalopram 10mg', 'Antidepressivo para ansiedade e depressao', 32.50, 1, 65),
('Sinvastatina 10mg', 'Estatina para colesterol', 25.90, 8, 85),
('Cetoconazol 2% Shampoo', 'Antifúngico para caspa', 28.50, 11, 75),
('Protetor Solar Infantil FPS 50', 'Protetor solar para criancas', 49.90, 11, 60),
('Multivitaminico Centrum', 'Complexo vitamínico completo', 65.50, 4, 50),
('Colageno Hidrolisado', 'Suplemento para pele, cabelo e unhas', 58.90, 4, 70),
('Creatina Monohidratada', 'Suplemento para performance muscular', 48.50, 4, 60),
('Whey Protein', 'Proteina para ganho de massa muscular', 89.90, 4, 40),
('Termometro Digital', 'Termometro clínico digital', 25.90, 5, 100),
('Algodao Higienico', 'Algodao para curativos e higiene', 8.50, 5, 300),
('Band-aid 50un', 'Curativos adesivos esterelizados', 12.50, 5, 200);

SELECT COUNT(*) as total_produtos FROM produtos;
