FROM gradle:7.6.4-jdk11 AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:11-jre
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=build /workspace/build/libs/boontory-backend-0.0.1-SNAPSHOT.jar app.jar
ENV BOONTORY_DB_PATH=/app/data/boontory.db
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
