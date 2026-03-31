# 🏥 ClickFarma - Farmácia Online

Sistema completo de e-commerce para farmácia com gestão de produtos, pedidos, rastreamento de entregas e dashboard administrativo.

## 🚀 Sobre o Projeto

O ClickFarma é uma plataforma de e-commerce especializada em produtos farmacêuticos, oferecendo:

- 🛒 Catálogo de produtos com categorias
- 👤 Cadastro e autenticação de usuários
- 🛍️ Carrinho de compras
- 💳 Processamento de pagamentos (simulado)
- 📦 Rastreamento de pedidos em tempo real
- 📊 Dashboard administrativo
- 🔐 Controle de acesso com JWT

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|--------|------------|
| **Backend** | Java 17, Spring Boot, Spring Security, Spring Data JPA, MySQL 8.0, Maven |
| **Frontend** | Vue.js 3, Vuex, Vue Router, Axios, Bootstrap 5 |
| **Infraestrutura** | Docker, Docker Compose, Nginx |

---

## 📦 Pré-requisitos

```bash
# Verificar instalações
docker --version      # Docker 20.10+
docker-compose --version
```

## 📁 Estrutura do Projeto
```bash
clickfarmafront/
├── backend/                 # Spring Boot
│   ├── src/main/java/       # Código Java
│   ├── src/main/resources/  # Configurações
│   ├── pom.xml             # Dependências Maven
│   └── Dockerfile.backend  # Dockerfile
├── frontend/                # Vue.js
│   ├── src/                # Código fonte
│   ├── public/             # Arquivos estáticos
│   ├── nginx.conf          # Configuração Nginx
│   ├── package.json        # Dependências Node
│   └── Dockerfile          # Dockerfile
├── docker-compose.yml      # Orquestração
└── README.md               # Documentação
```

## 🚀 Como Rodar
```bash
1. Clonar o repositório
git clone https://github.com/seu-usuario/clickfarmafront.git
cd clickfarmafront
```

## 2. Subir os containers
# Subir todos os serviços
```bash
docker compose up -d
sleep 10
docker ps
```

## 3. Acessar a aplicação
```bash
Frontend: http://localhost
API: http://localhost:8080
Swagger: http://localhost:8080/swagger-ui.html
```
