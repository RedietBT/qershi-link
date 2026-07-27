# Stage 1: Build with Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy the Root/Parent POM first to establish the reactor framework
COPY pom.xml ./pom.xml

# 2. Copy the shared library and the profile service directories
COPY common-library ./common-library
COPY profile-service ./profile-service

# 3. Build target service from the root level
RUN mvn clean package -pl profile-service -am -DskipTests

# Stage 2: Lightweight Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 4. Pull the compiled executable binary out of the build stage
COPY --from=build /app/profile-service/target/*.jar app.jar

EXPOSE 8081
EXPOSE 9081
ENTRYPOINT ["java", "-jar", "app.jar"]
