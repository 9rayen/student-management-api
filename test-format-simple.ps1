Write-Host "=== Testing Centralized JWT Service Response Format ===" -ForegroundColor Green

try {
    Write-Host "Testing token generation..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/jwt/generate" -Method POST -Body '{"username": "admin"}' -ContentType "application/json"
    
    Write-Host "Raw response:" -ForegroundColor White
    Write-Host ($response | ConvertTo-Json -Depth 4) -ForegroundColor Gray
    
    Write-Host "`nAnalyzing response structure..." -ForegroundColor Yellow
    Write-Host "Response type: $($response.GetType().Name)" -ForegroundColor Gray
    Write-Host "Properties: $($response.PSObject.Properties.Name -join ', ')" -ForegroundColor Gray
    
} catch {
    Write-Host "Failed to connect to centralized service:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Gray
    Write-Host "`nIs the centralized JWT service running on port 8091?" -ForegroundColor Yellow
}
