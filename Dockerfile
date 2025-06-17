FROM openjdk:21-jdk-slim

VOLUME /tmp
VOLUME /data

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Create directory for H2 database files
RUN mkdir -p /data

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", "-jar", "/app.jar"]
