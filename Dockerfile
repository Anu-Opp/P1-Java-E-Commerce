FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy any JAR file from target directory
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
