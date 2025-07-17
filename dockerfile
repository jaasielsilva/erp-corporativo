# Use uma imagem base oficial do OpenJDK 17 (JRE para rodar, JDK se precisar compilar)
FROM openjdk:17-jdk-slim

# Defina o diretório de trabalho dentro do container
WORKDIR /app

# Copie o arquivo JAR da sua aplicação para dentro do container
# Ajuste o nome do arquivo JAR conforme o seu projeto
COPY target/portal-ceo-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta que sua aplicação vai usar (exemplo 8080)
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]