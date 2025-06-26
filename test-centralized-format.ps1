# Test Centralized JWT Service Response Format
Write-Host "=== Testing Centralized JWT Service ===" -ForegroundColor Green

# Test what the centralized service actually returns
Write-Host "Testing token generation from centralized service..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/generate" -Method POST -Body '{"username": "admin"}' -ContentType "application/json"
    Write-Host "SUCCESS: Centralized service response:" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 4) -ForegroundColor White
    
    # Check the actual structure
    if ($response.PSObject.Properties.Name -contains "success") {
        Write-Host "✓ Has 'success' field: $($response.success)" -ForegroundColor Green
    } else {
        Write-Host "✗ Missing 'success' field" -ForegroundColor Red
    }
    
    if ($response.PSObject.Properties.Name -contains "data") {
        Write-Host "✓ Has 'data' field" -ForegroundColor Green
        if ($response.data.PSObject.Properties.Name -contains "token") {
            Write-Host "✓ Has 'data.token' field: $($response.data.token.Substring(0,20))..." -ForegroundColor Green
        } else {
            Write-Host "✗ Missing 'data.token' field" -ForegroundColor Red
        }
    } else {
        Write-Host "✗ Missing 'data' field" -ForegroundColor Red
    }
    
} catch {
    Write-Host "ERROR calling centralized service:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Gray
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
    }
}

# Test validation endpoint
Write-Host "`nTesting token validation..." -ForegroundColor Yellow
try {
    # First get a token
    $tokenResponse = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/generate" -Method POST -Body '{"username": "admin"}' -ContentType "application/json"
    $token = $tokenResponse.data.token
    
    # Now validate it
    $validationResponse = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/validate" -Method POST -Body "{`"token`": `"$token`"}" -ContentType "application/json"
    Write-Host "Validation response:" -ForegroundColor Green
    Write-Host ($validationResponse | ConvertTo-Json -Depth 3) -ForegroundColor White
    
} catch {
    Write-Host "Validation test failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Gray
}
