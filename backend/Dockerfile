# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# copy maven wrapper, pom, and source
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src

# build JAR (skip tests for faster image builds)
RUN ./mvnw clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# copy the jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# run the app
ENTRYPOINT ["java", "-jar", "app.jar"]