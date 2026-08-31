# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the maven wrapper and pom.xml first to cache dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

# Download dependencies (this step is cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy the project source
COPY src src

# Package the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Production-safe defaults; every secret and database setting remains external.
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
EXPOSE 8080

RUN addgroup -S app && adduser -S app -G app

# Copy the built JAR from the build stage
COPY --from=build --chown=app:app /app/target/facturacion-0.0.1-SNAPSHOT.jar app.jar

USER app

# Run the application with memory limits suitable for Render Free Tier (512MB)
CMD ["java", "-Xmx400m", "-jar", "app.jar"]
