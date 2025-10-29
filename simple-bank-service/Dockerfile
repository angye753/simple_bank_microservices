FROM openjdk:21-jdk-slim

# Set environment variable
ENV JAVA_OPTS=""

ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /usr/local/bin/wait-for-it.sh
RUN chmod +x /usr/local/bin/wait-for-it.sh

# Create a directory for the app
WORKDIR /app

RUN chmod -R a+w /app

RUN mkdir -p /app/uploads

# Copy the JAR into the image
COPY target/*.jar app.jar

# Expose the port your app runs on (usually 8080)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
