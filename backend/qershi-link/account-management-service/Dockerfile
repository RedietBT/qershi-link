FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
EXPOSE 9082
ENTRYPOINT ["java", "-jar", "app.jar"]
