# ===== Builder stage =====
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Csak POM -> dependency cache
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Források
COPY src ./src

# (Ha van) resources stb. jön a src alatt
# Build (tesztek nélkül a gyorsabb és determinisztikusabb image miatt)
RUN mvn -B -q -e -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# A jar nevét igazítsd a tiédhez (target/askmateoop-0.0.1-SNAPSHOT.jar)
COPY --from=builder /app/target/*.jar app.jar

# Ajánlott: nem root user (opcionális)
# RUN addgroup -S spring && adduser -S spring -G spring
# USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
