FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/loan-management-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8085 9085
ENTRYPOINT ["java", "-jar", "app.jar"]
