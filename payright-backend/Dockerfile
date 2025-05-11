FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
COPY --from=build /app/target/*SNAPSHOT.jar /app/PayRight.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/PayRight.jar"]
