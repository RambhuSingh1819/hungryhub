i
# Use Java 21  base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8185

# Run the jar
CMD ["java", "-jar", "target/*.jar"]
:wq
