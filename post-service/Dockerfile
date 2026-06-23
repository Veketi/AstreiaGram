# Build stage
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B \
    -Dmaven.wagon.http.retryHandler.count=5

COPY src ./src
RUN mvn clean package -DskipTests -B \
    -Dmaven.wagon.http.retryHandler.count=5

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

#ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["java", "-Dspring.data.mongodb.uri=mongodb://mongo:27017/postservice", "-jar", "app.jar"]
