# Etapa 1: compila a aplicação
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Compatibiliza o script com Linux e permite sua execução
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Aproveita o cache de dependências entre builds
RUN ./mvnw -B -ntp dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -ntp package

# Etapa 2: executa a aplicação
FROM eclipse-temurin:25-jre

WORKDIR /app

# Executa sem privilégios de root
RUN groupadd --system app && useradd --system --gid app app

COPY --from=build --chown=app:app /app/target/*.jar /app/app.jar

USER app

EXPOSE 8080

# Usa a porta fornecida pelo Render; localmente, utiliza 8080
ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --server.port=${PORT:-8080} --server.address=0.0.0.0"]