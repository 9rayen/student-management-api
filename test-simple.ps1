# Simple JWT Test using curl commands
Write-Host "=== Simple JWT Integration Test ===" -ForegroundColor Green

# Test 1: Login
Write-Host "`n1. Testing login..." -ForegroundColor Yellow
$loginResult = curl -X POST "http://localhost:8080/api/v1/auth/login" -H "Content-Type: application/json" -d '{\"username\": \"admin\", \"password\": \"admin123\"}'
Write-Host "Login result: $loginResult" -ForegroundColor Gray

# Extract token (simple regex)
if ($loginResult -match '"token":"([^"]+)"') {
    $token = $matches[1]
    Write-Host "Extracted token: $($token.Substring(0,30))..." -ForegroundColor Green
    
    # Test 2: Protected endpoint
    Write-Host "`n2. Testing protected endpoint..." -ForegroundColor Yellow
    $protectedResult = curl -X GET "http://localhost:8080/api/v1/student" -H "Authorization: Bearer $token"
    Write-Host "Protected endpoint result: $protectedResult" -ForegroundColor Gray
    
    # Test 3: Token validation
    Write-Host "`n3. Testing token validation..." -ForegroundColor Yellow
    $validateResult = curl -X POST "http://localhost:8080/api/v1/auth/validate" -H "Authorization: Bearer $token"
    Write-Host "Validation result: $validateResult" -ForegroundColor Gray
    
} else {
    Write-Host "Could not extract token from login response" -ForegroundColor Red
}
