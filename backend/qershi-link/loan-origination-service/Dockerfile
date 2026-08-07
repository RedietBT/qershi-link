FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8084 9084
ENTRYPOINT ["java", "-jar", "app.jar"]
