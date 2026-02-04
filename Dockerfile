# Use Java 21 base image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy only the built JAR
COPY target/*.jar app.jar

# Expose port
EXPOSE 8185

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]


