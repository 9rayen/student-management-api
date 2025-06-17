@echo off
echo Starting application in DEVELOPMENT mode with H2 in-memory database...
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run -Dspring.profiles.active=dev
