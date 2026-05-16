# syntax=docker/dockerfile:1.7
#
# Builda o módulo bootstrap `app` (core é biblioteca, sem main). No pacote
# `source` o POM já vem enxuto (sem profiles): `package` builda exatamente as
# features contratadas.

# ─── Stage 1: build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY . .
RUN chmod +x mvnw && ./mvnw -B -pl app -am -DskipTests clean package

# ─── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=builder /app/app/target/lpsoft.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
