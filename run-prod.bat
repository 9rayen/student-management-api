@echo off
echo Starting application in PRODUCTION mode with H2 file-based database...
set SPRING_PROFILES_ACTIVE=prod
set DB_USERNAME=sa
set DB_PASSWORD=prodPassword123
set JWT_SECRET=prodSecretKeyForJWTTokenGenerationAndValidationInStudentManagementAPI2025Production
set H2_CONSOLE_ENABLED=false
mvn spring-boot:run -Dspring.profiles.active=prod
