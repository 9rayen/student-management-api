# Environment Test Script
Write-Host "=== Spring Boot Multi-Environment Test ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check if Maven is available
Write-Host "1. Checking Maven installation..." -ForegroundColor Yellow
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "✓ Maven is installed" -ForegroundColor Green
} else {
    Write-Host "✗ Maven not found. Please install Maven first." -ForegroundColor Red
}

# Test 2: Check if project structure is correct
Write-Host ""
Write-Host "2. Checking project structure..." -ForegroundColor Yellow

$requiredFiles = @(
    "pom.xml",
    "src\main\resources\application.properties",
    "src\main\resources\application-dev.properties", 
    "src\main\resources\application-prod.properties",
    "run-dev.bat",
    "run-prod.bat",
    "run-dev.ps1",
    "run-prod.ps1",
    "setup-prod-dirs.bat",
    "backup-h2-db.bat",
    "Dockerfile",
    "docker-compose.yml"
)

foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "✓ $file exists" -ForegroundColor Green
    } else {
        Write-Host "✗ $file missing" -ForegroundColor Red
    }
}

# Test 3: Check directories
Write-Host ""
Write-Host "3. Checking directories..." -ForegroundColor Yellow

if (Test-Path "data") {
    Write-Host "✓ data directory exists" -ForegroundColor Green
} else {
    Write-Host "✗ data directory missing (run setup-prod-dirs.bat)" -ForegroundColor Red
}

if (Test-Path "logs") {
    Write-Host "✓ logs directory exists" -ForegroundColor Green
} else {
    Write-Host "✗ logs directory missing (run setup-prod-dirs.bat)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "To start the application:" -ForegroundColor White
Write-Host "  Development:  .\run-dev.ps1" -ForegroundColor Green
Write-Host "  Production:   .\run-prod.ps1" -ForegroundColor Yellow
Write-Host ""
Write-Host "Access points:" -ForegroundColor White
Write-Host "  Application:  http://localhost:8080" -ForegroundColor Cyan
Write-Host "  H2 Console:   http://localhost:8080/h2-console (dev only)" -ForegroundColor Cyan
Write-Host "  Swagger UI:   http://localhost:8080/swagger-ui.html (dev only)" -ForegroundColor Cyan
Write-Host "  Health:       http://localhost:8080/actuator/health" -ForegroundColor Cyan
