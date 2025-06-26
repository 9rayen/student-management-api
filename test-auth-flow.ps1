# Comprehensive JWT Authentication Flow Test
Write-Host "=== JWT Authentication Flow Debug ===" -ForegroundColor Green

# Step 1: Login and get token
Write-Host "`n1. Login to Student API..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body '{"username": "admin", "password": "admin123"}' -ContentType "application/json"
    
    Write-Host "✓ Login successful" -ForegroundColor Green
    $token = $loginResponse.token
    Write-Host "Token: $($token.Substring(0,50))..." -ForegroundColor Gray
    Write-Host "Token Type: $($loginResponse.type)" -ForegroundColor Gray
    Write-Host "Username: $($loginResponse.username)" -ForegroundColor Gray
    Write-Host "Expires In: $($loginResponse.expiresIn)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Test token validation directly
Write-Host "`n2. Test token validation endpoint..." -ForegroundColor Yellow
try {
    $headers = @{ "Authorization" = "Bearer $token" }
    $validationResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/validate" -Method POST -Headers $headers
    
    Write-Host "✓ Token validation response:" -ForegroundColor Green
    Write-Host ($validationResponse | ConvertTo-Json -Depth 2) -ForegroundColor White
} catch {
    Write-Host "✗ Token validation failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
    }
}

# Step 3: Test centralized service validation directly
Write-Host "`n3. Test centralized service validation directly..." -ForegroundColor Yellow
try {
    $centralValidationResponse = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/validate" -Method POST -Body "{`"token`": `"$token`"}" -ContentType "application/json"
    
    Write-Host "✓ Centralized validation response:" -ForegroundColor Green
    Write-Host ($centralValidationResponse | ConvertTo-Json -Depth 3) -ForegroundColor White
} catch {
    Write-Host "✗ Centralized validation failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 4: Test protected endpoint with detailed error info
Write-Host "`n4. Test protected endpoint access..." -ForegroundColor Yellow
try {
    $studentResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/student" -Headers $headers
    Write-Host "✓ SUCCESS! Protected endpoint accessible" -ForegroundColor Green
    Write-Host ($studentResponse | ConvertTo-Json -Depth 2) -ForegroundColor White
} catch {
    Write-Host "✗ Protected endpoint failed (403 error as expected)" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
    
    # Get detailed error response
    try {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Error response body: $errorBody" -ForegroundColor Gray
    } catch {
        Write-Host "Could not read error response body" -ForegroundColor Gray
    }
}

Write-Host "`n=== Analysis ===" -ForegroundColor Cyan
Write-Host "Based on the logs, the issue is in the JWT authentication filter." -ForegroundColor Yellow
Write-Host "The authentication is being set to 'anonymous' instead of authenticated." -ForegroundColor Yellow
Write-Host "`nNext steps:" -ForegroundColor White
Write-Host "1. Check if the centralized JWT service validation is working correctly" -ForegroundColor Gray
Write-Host "2. Verify the JwtAuthenticationFilter is properly parsing the centralized response" -ForegroundColor Gray
Write-Host "3. Check application logs for JWT validation errors" -ForegroundColor Gray
