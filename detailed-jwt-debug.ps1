# Detailed JWT Debug Test
Write-Host "=== DETAILED JWT AUTHENTICATION DEBUG ===" -ForegroundColor Cyan

# Step 1: Login and examine token content
Write-Host "`n[STEP 1] Login and examine token details..." -ForegroundColor Yellow
try {
    $loginBody = '{"username": "admin", "password": "admin123"}'
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    
    Write-Host "✓ Login successful!" -ForegroundColor Green
    $token = $loginResponse.token
    Write-Host "Full login response:" -ForegroundColor White
    Write-Host ($loginResponse | ConvertTo-Json -Depth 3) -ForegroundColor Gray
    
    # Decode JWT token payload (just for debugging - not secure but shows content)
    $tokenParts = $token.Split('.')
    if ($tokenParts.Length -eq 3) {
        $payload = $tokenParts[1]
        # Add padding if needed
        while ($payload.Length % 4 -ne 0) { $payload += "=" }
        try {
            $decodedBytes = [System.Convert]::FromBase64String($payload)
            $decodedJson = [System.Text.Encoding]::UTF8.GetString($decodedBytes)
            Write-Host "`nToken payload content:" -ForegroundColor Cyan
            Write-Host $decodedJson -ForegroundColor Gray
        } catch {
            Write-Host "Could not decode token payload" -ForegroundColor Yellow
        }
    }
    
} catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Test token validation
Write-Host "`n[STEP 2] Test token validation..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    
    $validationResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/validate" -Method POST -Headers $headers
    Write-Host "✓ Token validation response:" -ForegroundColor Green
    Write-Host ($validationResponse | ConvertTo-Json -Depth 2) -ForegroundColor White
    
} catch {
    Write-Host "✗ Token validation failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
    }
}

# Step 3: Test GET endpoint (should work)
Write-Host "`n[STEP 3] Test GET /api/v1/student (should work for ADMIN)..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $students = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/student" -Method GET -Headers $headers
    Write-Host "✓ GET request successful!" -ForegroundColor Green
    Write-Host "Number of students: $($students.Count)" -ForegroundColor White
    
} catch {
    Write-Host "✗ GET request failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
    }
}

# Step 4: Test POST endpoint with detailed error capture
Write-Host "`n[STEP 4] Test POST /api/v1/student (requires ADMIN role)..." -ForegroundColor Yellow
try {
    $newStudent = @{
        name = "Debug Test Student"
        email = "debug@test.com"
        age = 25
    } | ConvertTo-Json
    
    Write-Host "Request details:" -ForegroundColor Gray
    Write-Host "  URL: http://localhost:8080/api/v1/student" -ForegroundColor Gray
    Write-Host "  Method: POST" -ForegroundColor Gray
    Write-Host "  Authorization: Bearer $($token.Substring(0, 30))..." -ForegroundColor Gray
    Write-Host "  Body: $newStudent" -ForegroundColor Gray
    
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/student" -Method POST -Body $newStudent -Headers $headers
    
    Write-Host "✓ POST request successful!" -ForegroundColor Green
    Write-Host "Response: $($createResponse | ConvertTo-Json)" -ForegroundColor White
    
} catch {
    Write-Host "✗ POST request failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        # Try to capture detailed error response
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Detailed error response:" -ForegroundColor Red
            Write-Host $errorBody -ForegroundColor Gray
        } catch {
            Write-Host "Could not read detailed error response" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n=== ANALYSIS ===" -ForegroundColor Cyan
Write-Host "Key things to check:" -ForegroundColor White
Write-Host "1. Does the token contain the correct role (ADMIN)?" -ForegroundColor Gray
Write-Host "2. Is the token validation successful?" -ForegroundColor Gray
Write-Host "3. Are the Spring Security roles properly configured?" -ForegroundColor Gray
Write-Host "4. Is there a mismatch between token role and required role?" -ForegroundColor Gray

Write-Host "`nIf POST fails but GET works, it's likely a role authorization issue." -ForegroundColor Yellow
