FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /build

COPY . .


RUN mvn -pl analytic-service -am clean package
  ##

FROM eclipse-temurin:21-jdk AS runner

WORKDIR /app

COPY --from=builder ./build/analytic-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]