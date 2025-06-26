# JWT Integration Debug Script
# This script helps diagnose JWT integration issues between Student API and Centralized JWT Service

Write-Host "=== JWT Integration Debug Script ===" -ForegroundColor Green
Write-Host "Testing integration between Student API (8080) and Centralized JWT Service (8091)" -ForegroundColor Yellow

# Function to test if a service is running
function Test-ServiceRunning {
    param($port, $serviceName)
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$port" -Method GET -TimeoutSec 5 -ErrorAction Stop
        Write-Host "✓ $serviceName is running on port $port" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "✗ $serviceName is NOT running on port $port" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
        return $false
    }
}

# Test 1: Check if both services are running
Write-Host "`n=== Step 1: Service Availability Check ===" -ForegroundColor Cyan

$studentApiRunning = Test-ServiceRunning -port 8080 -serviceName "Student API"
$jwtServiceRunning = Test-ServiceRunning -port 8091 -serviceName "Centralized JWT Service"

if (-not $jwtServiceRunning) {
    Write-Host "`n🚨 CRITICAL: Centralized JWT Service is not running!" -ForegroundColor Red
    Write-Host "Please start the JWT service on port 8091 first." -ForegroundColor Yellow
    Write-Host "Expected location: ../JWT_CENTRALIZE/ folder" -ForegroundColor Gray
    exit 1
}

if (-not $studentApiRunning) {
    Write-Host "`n🚨 CRITICAL: Student API is not running!" -ForegroundColor Red
    Write-Host "Please start the Student API on port 8080 first." -ForegroundColor Yellow
    Write-Host "Run: mvn spring-boot:run" -ForegroundColor Gray
    exit 1
}

# Test 2: Test centralized JWT service directly
Write-Host "`n=== Step 2: Direct Centralized JWT Service Test ===" -ForegroundColor Cyan

try {
    Write-Host "Testing direct token generation from centralized service..." -ForegroundColor Yellow
    
    $jwtRequestBody = '{"username": "admin"}'
    
    Write-Host "Request payload: $jwtRequestBody" -ForegroundColor Gray
    
    $response = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/generate" -Method POST -Body $jwtRequestBody -ContentType "application/json" -TimeoutSec 10
    
    Write-Host "✓ Centralized JWT service response:" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor White
    
    if ($response.success -and $response.data -and $response.data.token) {
        $centralizedToken = $response.data.token
        Write-Host "✓ Token generated successfully: $($centralizedToken.Substring(0, 20))..." -ForegroundColor Green
    } else {
        Write-Host "✗ Unexpected response format from centralized service" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Failed to get token from centralized service" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
    exit 1
}

# Test 3: Test Student API login
Write-Host "`n=== Step 3: Student API Login Test ===" -ForegroundColor Cyan

try {
    Write-Host "Testing login via Student API..." -ForegroundColor Yellow
    
    $loginRequestBody = '{"username": "admin", "password": "admin123"}'
    
    Write-Host "Login request: $loginRequestBody" -ForegroundColor Gray
    
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $loginRequestBody -ContentType "application/json" -TimeoutSec 10
    
    Write-Host "✓ Login response:" -ForegroundColor Green
    Write-Host ($loginResponse | ConvertTo-Json -Depth 2) -ForegroundColor White
    
    if ($loginResponse.token) {
        $studentApiToken = $loginResponse.token
        Write-Host "✓ Login successful, token: $($studentApiToken.Substring(0, 20))..." -ForegroundColor Green
    } else {
        Write-Host "✗ No token in login response" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Login failed via Student API" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
    exit 1
}

# Test 4: Test protected endpoint access
Write-Host "`n=== Step 4: Protected Endpoint Access Test ===" -ForegroundColor Cyan

try {
    Write-Host "Testing access to protected endpoint /api/v1/student..." -ForegroundColor Yellow
    
    $headers = @{
        "Authorization" = "Bearer $studentApiToken"
        "Content-Type" = "application/json"
    }
    
    Write-Host "Using token: $($studentApiToken.Substring(0, 20))..." -ForegroundColor Gray
    
    $studentResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/student" -Method GET -Headers $headers -TimeoutSec 10
    
    Write-Host "✓ Protected endpoint access successful!" -ForegroundColor Green
    Write-Host "Response: $($studentResponse | ConvertTo-Json -Depth 2)" -ForegroundColor White
    
} catch {
    Write-Host "✗ Protected endpoint access failed - THIS IS THE 403 ERROR" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
    
    # Let's test token validation directly
    Write-Host "`n--- Token Validation Debug ---" -ForegroundColor Yellow
    
    try {
        Write-Host "Testing token validation endpoint..." -ForegroundColor Yellow
        $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/validate" -Method POST -Headers $headers -TimeoutSec 10
        Write-Host "Token validation response:" -ForegroundColor White
        Write-Host ($validateResponse | ConvertTo-Json -Depth 2) -ForegroundColor White
    } catch {
        Write-Host "Token validation also failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 5: Check application logs
Write-Host "`n=== Step 5: Recommendations ===" -ForegroundColor Cyan

Write-Host "If the 403 error persists, check the following:" -ForegroundColor Yellow
Write-Host "1. Ensure the centralized JWT service expects exactly: {username: 'value'}" -ForegroundColor White
Write-Host "2. Verify the centralized service returns: {success: true, data: {token, type, expiresIn}}" -ForegroundColor White
Write-Host "3. Check if the token validation endpoint in centralized service is working" -ForegroundColor White
Write-Host "4. Review application logs for JWT validation errors" -ForegroundColor White
Write-Host "5. Test with centralized service disabled: jwt.enable-centralized-service=false" -ForegroundColor White

Write-Host "`n=== Debug Complete ===" -ForegroundColor Green
