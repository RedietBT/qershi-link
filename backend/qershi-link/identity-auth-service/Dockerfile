# Stage 1: Build with Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy the Root/Parent POM first to establish the reactor framework
COPY pom.xml ./pom.xml

# 2. Copy the shared library and the auth service directories
COPY common-library ./common-library
COPY identity-auth-service ./identity-auth-service

# 3. Build target service from the root level
# -pl (project-list): Tells Maven to focus strictly on identity-auth-service
# -am (also-make): Tells Maven to automatically locate and build internal dependencies (common-library)
RUN mvn clean package -pl identity-auth-service -am -DskipTests

# Stage 2: Lightweight Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 4. Pull the compiled executable binary out of the build stage
COPY --from=build /app/identity-auth-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]