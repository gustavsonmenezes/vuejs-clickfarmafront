# Estágio de Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom.xml e os wrappers do Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixa as dependências do projeto
RUN ./mvnw dependency:go-offline

# Copia o código-fonte do backend
COPY src ./src

# Compila o projeto e gera o .jar
RUN ./mvnw package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copia o .jar do estágio de build
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar .

# Expõe a porta que a aplicação Spring Boot usa
EXPOSE 8080

# Comando para iniciar a aplicação
CMD ["java", "-jar", "backend-0.0.1-SNAPSHOT.jar"]
