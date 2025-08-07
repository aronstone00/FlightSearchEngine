# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
# Build the executable fat-jar (skip tests for speed)
RUN mvn -q clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy Spring Boot fat-jar from the build stage
COPY --from=build /workspace/target/flight-search-poc-1.0.0.jar app.jar
# Default HTTP port
EXPOSE 8080
# Health check (optional, Docker 18.06+)
HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:8080/healthz || exit 1
# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
