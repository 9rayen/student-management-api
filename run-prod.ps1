# PowerShell script to run production environment
Write-Host "Starting application in PRODUCTION mode with H2 file-based database..." -ForegroundColor Yellow
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DB_USERNAME = "sa"
$env:DB_PASSWORD = "prodPassword123"
$env:JWT_SECRET = "prodSecretKeyForJWTTokenGenerationAndValidationInStudentManagementAPI2025Production"
$env:H2_CONSOLE_ENABLED = "false"
mvn spring-boot:run "-Dspring.profiles.active=prod"
