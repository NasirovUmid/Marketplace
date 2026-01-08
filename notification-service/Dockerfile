FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /build

COPY . .



RUN mvn clean package -pl notification-service -am -DskipTests
  ##

FROM eclipse-temurin:21-jdk AS runner

WORKDIR /app

COPY --from=builder ./build/notification-service/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]