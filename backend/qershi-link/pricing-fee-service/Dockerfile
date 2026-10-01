FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8087
EXPOSE 9087
ENTRYPOINT ["java", "-jar", "app.jar"]
