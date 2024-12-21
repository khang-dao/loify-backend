# Stage 1: Build the JAR
FROM maven:3.8-openjdk-17 AS jar-builder
WORKDIR /app
COPY . .
RUN mvn clean package

# Stage 2: Create image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=jar-builder /app/target/loify-api.jar /app/loify-api.jar
EXPOSE 8080
RUN apt-get update && apt-get install -y libfreetype6 libfontconfig1
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "/app/loify-api.jar"]
