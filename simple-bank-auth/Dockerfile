FROM openjdk:21-jdk-slim

# Set environment variable
ENV JAVA_OPTS=""

# Create a directory for the app
WORKDIR /app

RUN chmod -R a+w /app

RUN mkdir -p /app/uploads

# Copy the JAR into the image
COPY target/*.jar app_read.jar

# Expose the port your app runs on (usually 8080)
EXPOSE 8082

# Run the app
ENTRYPOINT ["java", "-jar", "app_read.jar"]
