# Stage 1: Build Vue.js frontend
FROM node:25-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
RUN npm run build

# Stage 2: Build Spring Boot with embedded frontend
FROM gradle:7.6.4-jdk11 AS backend-build
WORKDIR /workspace
COPY backend/ .
COPY --from=frontend-build /app/dist ./src/main/resources/static
RUN ./gradlew bootJar --no-daemon

# Stage 3: Run
FROM eclipse-temurin:11-jre
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=backend-build /workspace/build/libs/boontory-backend-0.0.1-SNAPSHOT.jar app.jar
ENV BOONTORY_DB_PATH=/app/data/boontory.db
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
