# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the maven wrapper and pom.xml first to cache dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this step is cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy the project source
COPY src src

# Package the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Expose the port for Render (dynamic via PORT env var)
ENV PORT=8080
EXPOSE $PORT

# Copy the built JAR from the build stage
COPY --from=build /app/target/facturacion-0.0.1-SNAPSHOT.jar app.jar

# Run the application with memory limits suitable for Render Free Tier (512MB)
CMD ["java", "-Xmx400m", "-jar", "app.jar"]
