FROM openjdk:17
COPY target/ecommerce-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
