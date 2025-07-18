# Usa imagem base oficial OpenJDK 17 (JDK Slim)
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o arquivo JAR para dentro do container (ajuste o nome se necessário)
COPY target/portal-ceo-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta que a aplicação vai usar
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
