# Stage 1: Build Vue.js frontend
FROM node:25-alpine AS frontend-build
WORKDIR /app
ARG VITE_API_BASE_URL=
ARG VITE_KEYCLOAK_URL=https://auth.etharlia.com
ARG VITE_KEYCLOAK_REALM=etharlia
ARG VITE_KEYCLOAK_CLIENT_ID=boontory-frontend
ARG VITE_SSO_LOGOUT_URL=https://oauth.etharlia.com/oauth2/sign_out
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ENV VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM
ENV VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID
ENV VITE_SSO_LOGOUT_URL=$VITE_SSO_LOGOUT_URL
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
