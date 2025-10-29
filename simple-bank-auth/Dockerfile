FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean package -DskipTests

FROM openjdk:21-jdk-slim

# Set environment variable
ENV JAVA_OPTS=""

# Create a directory for the app
WORKDIR /app

RUN chmod -R a+w /app

RUN mkdir -p /app/uploads

# Copy the JAR into the image
COPY --from=builder /app/target/*.jar app_auth.jar
# Expose the port your app runs on (usually 8080)
EXPOSE 8082

# Run the app
ENTRYPOINT ["java", "-jar", "app_auth.jar"]
