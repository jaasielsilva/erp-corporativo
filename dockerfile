# =========================
# Etapa 1: Build da aplicação (gera o .jar)
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom.xml e baixa dependências (cache mais eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código-fonte e faz o build
COPY src ./src
RUN mvn clean package -DskipTests

# =========================
# Etapa 2: Imagem final para rodar o app
# =========================
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copia o .jar gerado da etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta da aplicação
EXPOSE 8080

# Comando para rodar o app
ENTRYPOINT ["java", "-jar", "app.jar"]