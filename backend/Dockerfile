# ===== Builder stage =====
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Step 1: only copy pom.xml and download deps (cache layer)
COPY pom.xml .
RUN mvn -B -q -e -Dmaven.test.skip=true dependency:go-offline

# Step 2: now copy actual sources
COPY src ./src

# Step 3: build the jar (now sources are present!)
RUN mvn -B -q -e -Dmaven.test.skip=true package

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Jar file name
COPY --from=builder /app/target/*.jar app.jar

# Recommended: non-root user (optional)
# RUN addgroup -S spring && adduser -S spring -G spring
# USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
