FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY swagger-api-hub/target/*.jar app.jar
EXPOSE 9020
ENTRYPOINT ["java", "-jar", "app.jar"]
