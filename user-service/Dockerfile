# ── Build stage ──────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copia apenas o pom primeiro para criar o cache de dependências do Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B \
    -Dmaven.wagon.http.retryHandler.count=5

# Copia o código fonte (se mudar o código, não rebaixa as dependências)
COPY src ./src
RUN mvn clean package -DskipTests -B \
    -Dmaven.wagon.http.retryHandler.count=5

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
