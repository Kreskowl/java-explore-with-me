FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/ewm-main-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]