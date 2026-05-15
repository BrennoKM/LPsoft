# syntax=docker/dockerfile:1.7

# ─── Stage 1: build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copia wrapper e POMs primeiro para aproveitar cache de dependências
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY core/pom.xml core/pom.xml

RUN chmod +x mvnw && ./mvnw -B -pl core -am dependency:go-offline -DskipTests || true

# Copia o código e empacota
COPY core/src core/src
RUN ./mvnw -B -pl core -am package -DskipTests

# ─── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=builder /app/core/target/core-*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
