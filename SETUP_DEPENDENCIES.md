# 📦 Guia de Instalação de Dependências

## 🎯 Objetivo

Este guia descreve como instalar as dependências necessárias para as novas funcionalidades de IA generativa.

---

## 📋 Dependências Necessárias

### Frontend (Vue.js)

#### Biblioteca Marked (Markdown Parser)
Necessária para renderizar as respostas da IA em Markdown formatado.

**Instalação:**
```bash
cd frontend
npm install marked
```

**Verificação:**
```bash
npm list marked
```

**Versão recomendada:** `^4.0.0` ou superior

**Uso no código:**
```javascript
import { marked } from 'marked'

// Renderizar Markdown
const html = marked('# Título\n\n**Negrito**')
```

---

### Backend (Spring Boot)

#### Dependências Já Incluídas
O backend já possui todas as dependências necessárias no `pom.xml`:

✅ **Spring Boot WebFlux** - Para requisições assíncronas
✅ **Google Cloud Vertex AI** - Para chamadas ao Gemini
✅ **Gson** - Para processamento JSON
✅ **Spring Data JPA** - Para persistência

**Verificação:**
```bash
cd backend
mvn dependency:tree | grep -E "vertexai|gson|webflux"
```

---

## 🚀 Passos de Instalação

### 1. Clonar o Repositório (Se necessário)
```bash
git clone https://github.com/gustavsonmenezes/vuejs-clickfarmafront.git
cd vuejs-clickfarmafront
```

### 2. Instalar Dependências do Frontend
```bash
cd frontend
npm install
```

**Saída esperada:**
```
added X packages in Ys
```

### 3. Instalar Dependências do Backend
```bash
cd ../backend
mvn clean install
```

**Saída esperada:**
```
[INFO] BUILD SUCCESS
```

### 4. Verificar Instalação

#### Frontend
```bash
cd frontend
npm list marked
```

Saída esperada:
```
clickfarma-frontend@1.0.0 /path/to/frontend
└── marked@4.x.x
```

#### Backend
```bash
cd backend
mvn dependency:tree | grep -i "google-cloud-vertexai"
```

Saída esperada:
```
[INFO] com.google.cloud:google-cloud-vertexai:jar:1.5.0:compile
```

---

## 🔧 Configuração de Variáveis de Ambiente

### Backend - Chave da API Gemini

A chave da API Gemini deve ser configurada como variável de ambiente:

#### Linux/Mac
```bash
export GEMINI_API_KEY="sua-chave-aqui"
```

#### Windows (PowerShell)
```powershell
$env:GEMINI_API_KEY="sua-chave-aqui"
```

#### Windows (CMD)
```cmd
set GEMINI_API_KEY=sua-chave-aqui
```

#### Docker
Adicionar ao `docker-compose.yml`:
```yaml
environment:
  GEMINI_API_KEY: ${GEMINI_API_KEY}
```

**Obter a chave:**
1. Acesse [Google AI Studio](https://aistudio.google.com/app/apikeys)
2. Clique em "Create API Key"
3. Copie a chave gerada
4. Configure como variável de ambiente

---

## 📝 Arquivo package.json Atualizado

Seu `frontend/package.json` deve incluir:

```json
{
  "dependencies": {
    "axios": "^1.4.0",
    "bootstrap": "^5.3.0",
    "leaflet": "^1.9.4",
    "marked": "^4.0.0",
    "pinia": "^3.0.4",
    "vue": "^3.3.0",
    "vue-router": "^4.2.0",
    "vue-toastification": "^2.0.0-rc.5",
    "vuex": "^4.0.2"
  }
}
```

**Se não estiver presente, adicione manualmente:**
```bash
cd frontend
npm install marked@^4.0.0
```

---

## 🧪 Teste de Funcionalidade

### 1. Iniciar o Backend
```bash
cd backend
mvn spring-boot:run
```

Saída esperada:
```
[INFO] Application 'backend' is running! Access URLs:
[INFO]   Local:      http://localhost:8080
```

### 2. Iniciar o Frontend (em outro terminal)
```bash
cd frontend
npm run dev
```

Saída esperada:
```
  VITE v4.x.x  ready in XXX ms

  ➜  Local:   http://localhost:5173/
```

### 3. Testar o Endpoint de Análise
```bash
curl -X POST http://localhost:8080/api/gemini/analyze-cart \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "id": 1,
        "name": "Paracetamol 500mg",
        "description": "Analgésico",
        "price": 12.50,
        "quantity": 2,
        "category": "Medicamentos"
      }
    ],
    "totalPrice": 25.00
  }'
```

Saída esperada:
```json
{
  "analysis": "## Análise do Seu Carrinho\n\n### Economia Potencial\n..."
}
```

### 4. Testar no Frontend
1. Acesse http://localhost:5173/
2. Adicione itens ao carrinho
3. Vá para a página do carrinho
4. Clique em "Analisar Carrinho"
5. Aguarde a análise aparecer

---

## ⚠️ Troubleshooting

### Erro: "marked is not defined"

**Solução:**
```bash
npm install marked
```

Verifique se o import está correto:
```javascript
import { marked } from 'marked'
```

### Erro: "Chave da API Gemini não configurada"

**Solução:**
```bash
export GEMINI_API_KEY="sua-chave-aqui"
mvn spring-boot:run
```

Verifique se a variável está configurada:
```bash
echo $GEMINI_API_KEY
```

### Erro: "Connection refused" (Backend)

**Solução:**
1. Verifique se o backend está rodando: `lsof -i :8080`
2. Inicie o backend: `mvn spring-boot:run`
3. Aguarde a mensagem "Application is running"

### Erro: "CORS error"

**Solução:**
O CORS já está configurado no backend. Se persistir:
1. Verifique se `@CrossOrigin(origins = "*")` está no controller
2. Reinicie o backend

---

## 📊 Verificação de Instalação Completa

Execute este script para verificar tudo:

### Linux/Mac
```bash
#!/bin/bash

echo "🔍 Verificando instalação..."
echo ""

echo "✅ Node.js:"
node --version

echo "✅ npm:"
npm --version

echo "✅ Java:"
java -version

echo "✅ Maven:"
mvn --version

echo ""
echo "📦 Dependências do Frontend:"
cd frontend
npm list marked axios vue

echo ""
echo "📦 Dependências do Backend:"
cd ../backend
mvn dependency:tree | grep -E "vertexai|gson|webflux" | head -5

echo ""
echo "✅ Instalação verificada com sucesso!"
```

### Windows (PowerShell)
```powershell
Write-Host "🔍 Verificando instalação..." -ForegroundColor Green
Write-Host ""

Write-Host "✅ Node.js:" -ForegroundColor Green
node --version

Write-Host "✅ npm:" -ForegroundColor Green
npm --version

Write-Host "✅ Java:" -ForegroundColor Green
java -version

Write-Host "✅ Maven:" -ForegroundColor Green
mvn --version

Write-Host ""
Write-Host "📦 Dependências do Frontend:" -ForegroundColor Green
cd frontend
npm list marked axios vue

Write-Host ""
Write-Host "✅ Instalação verificada com sucesso!" -ForegroundColor Green
```

---

## 🎓 Próximos Passos

Após instalar as dependências:

1. ✅ Leia o arquivo `IA_IMPROVEMENTS.md` para entender as funcionalidades
2. ✅ Explore os componentes em `frontend/src/components/cart/`
3. ✅ Estude o backend em `backend/src/main/java/com/clickfarma/backend/`
4. ✅ Execute os testes: `npm run test` e `mvn test`

---

## 📚 Documentação Adicional

- [Marked.js Documentation](https://marked.js.org/)
- [Google Gemini API](https://ai.google.dev/)
- [Spring Boot WebFlux](https://spring.io/projects/spring-boot)
- [Vue.js 3 Guide](https://vuejs.org/guide/introduction.html)

---

## 💡 Dicas

1. **Use `npm ci` em produção** em vez de `npm install` para garantir versões exatas
2. **Sempre execute `mvn clean install`** antes de fazer push no Git
3. **Configure a chave da API em um arquivo `.env`** (não commite no Git!)
4. **Use `npm audit`** para verificar vulnerabilidades

---

**Última atualização**: Abril 2026
**Status**: ✅ Pronto para uso
