# ---------- BUILD STAGE ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY . .
RUN ./mvnw clean package -DskipTests

# ---------- RUN STAGE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]



