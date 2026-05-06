# Stage 1: Build with Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy the Parent POM first (needed for version management)
COPY pom.xml ./pom.xml

# 2. Copy the Identity Service files using their relative paths from root
COPY identity-auth-service/pom.xml ./identity-auth-service/pom.xml
COPY identity-auth-service/src ./identity-auth-service/src

# 3. Build using the child POM, but Maven will now see the Parent in the folder above
RUN mvn -f identity-auth-service/pom.xml clean package -DskipTests

# Stage 2: Run with JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 4. Copy the jar from the child's target folder
COPY --from=build /app/identity-auth-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]