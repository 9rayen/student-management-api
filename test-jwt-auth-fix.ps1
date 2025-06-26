# Test JWT Authentication System
# This script tests both centralized and local JWT authentication

Write-Host "Testing JWT Authentication System..." -ForegroundColor Green

# Wait for application to start
Start-Sleep -Seconds 5

$baseUrl = "http://localhost:8080"
$authEndpoint = "$baseUrl/api/v1/auth/login"

# Test credentials
$credentials = @{
    username = "admin"
    password = "admin123"
}

$body = $credentials | ConvertTo-Json
$headers = @{
    "Content-Type" = "application/json"
}

try {
    Write-Host "Testing authentication with local JWT generation..." -ForegroundColor Yellow
    
    # Test login
    $response = Invoke-RestMethod -Uri $authEndpoint -Method POST -Body $body -Headers $headers
    
    if ($response.token) {
        Write-Host "✓ Authentication successful!" -ForegroundColor Green
        Write-Host "  Token: $($response.token.Substring(0, 50))..." -ForegroundColor Cyan
        Write-Host "  Username: $($response.username)" -ForegroundColor Cyan
        Write-Host "  Expires In: $($response.expiresIn)" -ForegroundColor Cyan
        
        # Test protected endpoint
        $authHeader = @{
            "Authorization" = "Bearer $($response.token)"
            "Content-Type" = "application/json"
        }
        
        $studentsEndpoint = "$baseUrl/api/v1/student"
        $studentsResponse = Invoke-RestMethod -Uri $studentsEndpoint -Method GET -Headers $authHeader
        
        Write-Host "✓ Protected endpoint access successful!" -ForegroundColor Green
        Write-Host "  Retrieved $($studentsResponse.Count) students" -ForegroundColor Cyan
        
    } else {
        Write-Host "✗ Authentication failed - no token received" -ForegroundColor Red
    }
    
} catch {
    Write-Host "✗ Authentication test failed: $($_.Exception.Message)" -ForegroundColor Red
    
    # Try to get more details from the response
    if ($_.Exception.Response) {
        $responseStream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($responseStream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Gray
    }
}

Write-Host "JWT Authentication test completed." -ForegroundColor Green
