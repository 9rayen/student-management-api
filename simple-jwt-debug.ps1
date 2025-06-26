# Simple JWT Debug Script
Write-Host "=== JWT Integration Debug ===" -ForegroundColor Green

# Test 1: Check if centralized JWT service is running
Write-Host "`nStep 1: Testing Centralized JWT Service (port 8091)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8091" -TimeoutSec 5
    Write-Host "✓ Centralized JWT Service is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Centralized JWT Service is NOT running on port 8091" -ForegroundColor Red
    Write-Host "This is likely the cause of your 403 error!" -ForegroundColor Red
    Write-Host "Please start the JWT service first." -ForegroundColor Yellow
    exit 1
}

# Test 2: Test direct token generation from centralized service
Write-Host "`nStep 2: Testing direct token generation..." -ForegroundColor Yellow
try {
    $body = '{"username": "admin"}'
    $centralResponse = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/generate" -Method POST -Body $body -ContentType "application/json"
    Write-Host "✓ Direct token generation successful" -ForegroundColor Green
    Write-Host "Response format:" -ForegroundColor White
    Write-Host ($centralResponse | ConvertTo-Json -Depth 3) -ForegroundColor Gray
} catch {
    Write-Host "✗ Direct token generation failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
}

# Test 3: Test Student API login
Write-Host "`nStep 3: Testing Student API login..." -ForegroundColor Yellow
try {
    $loginBody = '{"username": "admin", "password": "admin123"}'
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    Write-Host "✓ Student API login successful" -ForegroundColor Green
    $token = $loginResponse.token
    Write-Host "Token received: $($token.Substring(0, 30))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Student API login failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
    exit 1
}

# Test 4: Test protected endpoint with token
Write-Host "`nStep 4: Testing protected endpoint..." -ForegroundColor Yellow
try {
    $headers = @{ "Authorization" = "Bearer $token" }
    $studentResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/student" -Headers $headers
    Write-Host "✓ Protected endpoint access successful!" -ForegroundColor Green
} catch {
    Write-Host "✗ Protected endpoint failed (403 error)" -ForegroundColor Red
    Write-Host "This confirms the token validation issue" -ForegroundColor Red
    
    # Test token validation specifically
    Write-Host "`nTesting token validation..." -ForegroundColor Yellow
    try {
        $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/validate" -Method POST -Headers $headers
        Write-Host "Token validation response:" -ForegroundColor White
        Write-Host ($validateResponse | ConvertTo-Json) -ForegroundColor Gray
    } catch {
        Write-Host "Token validation failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Quick Fix Suggestions ===" -ForegroundColor Cyan
Write-Host "1. Ensure JWT service is running on port 8091" -ForegroundColor White
Write-Host "2. Check if centralized service validation endpoint works" -ForegroundColor White
Write-Host "3. Try disabling centralized service temporarily:" -ForegroundColor White
Write-Host "   Set jwt.enable-centralized-service=false" -ForegroundColor Gray
Write-Host "4. Check application logs for detailed errors" -ForegroundColor White
