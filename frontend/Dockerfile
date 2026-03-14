# Estágio de Build
FROM node:18 AS build
WORKDIR /app

# Copia os arquivos de manifesto do projeto
COPY package.json ./
COPY package-lock.json ./

# Instala as dependências
RUN npm install

# Copia todo o código-fonte do frontend
COPY . .

# Builda a aplicação para produção (opcional, mas boa prática)
# RUN npm run build

# Estágio de Execução
FROM node:18
WORKDIR /app

# Copia as dependências e o código do estágio de build
COPY --from=build /app .

# Expõe a porta do servidor de desenvolvimento
EXPOSE 8080

# Comando para iniciar o servidor de desenvolvimento
CMD ["npm", "run", "serve"]
