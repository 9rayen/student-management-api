Write-Host "=== Testing JWT Validation Endpoint ===" -ForegroundColor Green

try {
    # First get a token
    Write-Host "1. Getting token from centralized service..." -ForegroundColor Yellow
    $tokenResponse = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/generate" -Method POST -Body '{"username": "admin"}' -ContentType "application/json"
    $token = $tokenResponse.data.token
    Write-Host "Got token: $($token.Substring(0,30))..." -ForegroundColor Gray
    
    # Now test validation
    Write-Host "`n2. Testing validation endpoint..." -ForegroundColor Yellow
    $validationBody = @{ token = $token } | ConvertTo-Json
    Write-Host "Validation request: $validationBody" -ForegroundColor Gray
    
    $validationResponse = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/validate" -Method POST -Body $validationBody -ContentType "application/json"
    
    Write-Host "Validation response:" -ForegroundColor Green
    Write-Host ($validationResponse | ConvertTo-Json -Depth 3) -ForegroundColor White
    
} catch {
    Write-Host "Validation test failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Gray
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
    }
}
