# PowerShell script to run development environment
Write-Host "Starting application in DEVELOPMENT mode with H2 in-memory database..." -ForegroundColor Green
$env:SPRING_PROFILES_ACTIVE = "dev"
mvn spring-boot:run "-Dspring.profiles.active=dev"
