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
# Build stage
FROM node:18-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production stage
FROM nginx:stable-alpine
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
