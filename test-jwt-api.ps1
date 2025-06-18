# Centralized JWT API Testing Script (PowerShell)
# This script tests all the JWT management endpoints

$BaseUrl = "http://localhost:8080/api/v1/jwt"
$ContentType = "application/json"

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  Centralized JWT API Testing Script" -ForegroundColor Yellow  
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

# Function to print colored output
function Write-Success {
    param($Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Error {
    param($Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Write-Info {
    param($Message)
    Write-Host "ℹ $Message" -ForegroundColor Blue
}

function Write-Header {
    param($Message)
    Write-Host ""
    Write-Host $Message -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Yellow
}

# Test 1: Check Service Status
Write-Header "1. Testing Service Status"
Write-Host "GET $BaseUrl/status"

try {
    $StatusResponse = Invoke-RestMethod -Uri "$BaseUrl/status" -Method Get -ContentType $ContentType
    Write-Success "Service status check passed"
    Write-Host "Response:"
    $StatusResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Error "Service status check failed: $($_.Exception.Message)"
}

# Test 2: Generate JWT Token (Login)
Write-Header "2. Testing Token Generation (Login)"

$LoginData = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

Write-Host "POST $BaseUrl/generate"
Write-Host "Data: $LoginData"

try {
    $LoginResponse = Invoke-RestMethod -Uri "$BaseUrl/generate" -Method Post -Body $LoginData -ContentType $ContentType
    Write-Success "Token generation passed"
    
    $JwtToken = $LoginResponse.data.token
    if ($JwtToken) {
        Write-Info "JWT Token obtained: $($JwtToken.Substring(0, [Math]::Min(50, $JwtToken.Length)))..."
        Write-Host "Full Response:"
        $LoginResponse | ConvertTo-Json -Depth 10
    } else {
        Write-Error "No JWT token in response"
        $JwtToken = $null
    }
} catch {
    Write-Error "Token generation failed: $($_.Exception.Message)"
    $JwtToken = $null
}

# Only continue with other tests if we have a valid token
if (-not $JwtToken) {
    Write-Error "Cannot continue without valid JWT token"
    exit 1
}

# Test 3: Validate Token (POST method)
Write-Header "3. Testing Token Validation (POST)"

$ValidateData = @{
    token = $JwtToken
    username = "admin"
} | ConvertTo-Json

Write-Host "POST $BaseUrl/validate"
Write-Host "Data: $ValidateData"

try {
    $ValidateResponse = Invoke-RestMethod -Uri "$BaseUrl/validate" -Method Post -Body $ValidateData -ContentType $ContentType
    Write-Success "Token validation (POST) passed"
    
    if ($ValidateResponse.data.valid -eq $true) {
        Write-Success "Token is valid"
    } else {
        Write-Error "Token validation returned false"
    }
    
    Write-Host "Response:"
    $ValidateResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Error "Token validation (POST) failed: $($_.Exception.Message)"
}

# Test 4: Validate Token (GET method with header)
Write-Header "4. Testing Token Validation (GET with Authorization header)"
Write-Host "GET $BaseUrl/validate"
Write-Host "Authorization: Bearer $JwtToken"

try {
    $Headers = @{
        "Authorization" = "Bearer $JwtToken"
    }
    
    $ValidateGetResponse = Invoke-RestMethod -Uri "$BaseUrl/validate" -Method Get -Headers $Headers
    Write-Success "Token validation (GET) passed"
    
    if ($ValidateGetResponse.data.valid -eq $true) {
        Write-Success "Token is valid via header"
    } else {
        Write-Error "Token validation via header returned false"
    }
    
    Write-Host "Response:"
    $ValidateGetResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Error "Token validation (GET) failed: $($_.Exception.Message)"
}

# Test 5: User Token Statistics
Write-Header "5. Testing User Token Statistics"
Write-Host "GET $BaseUrl/user/admin/stats"

try {
    $StatsResponse = Invoke-RestMethod -Uri "$BaseUrl/user/admin/stats" -Method Get
    Write-Success "User token statistics passed"
    
    $TokenCount = $StatsResponse.data.activeTokenCount
    Write-Info "Active tokens for user 'admin': $TokenCount"
    
    Write-Host "Response:"
    $StatsResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Error "User token statistics failed: $($_.Exception.Message)"
}

# Test 6: Revoke Token
Write-Header "6. Testing Token Revocation"

$RevokeData = @{
    token = $JwtToken
    reason = "API testing"
    revokeAllUserTokens = $false
} | ConvertTo-Json

Write-Host "POST $BaseUrl/revoke"
Write-Host "Data: $RevokeData"

try {
    $RevokeResponse = Invoke-RestMethod -Uri "$BaseUrl/revoke" -Method Post -Body $RevokeData -ContentType $ContentType
    Write-Success "Token revocation passed"
    
    if ($RevokeResponse.data.revoked -eq $true) {
        Write-Success "Token successfully revoked"
    } else {
        Write-Error "Token revocation returned false"
    }
    
    Write-Host "Response:"
    $RevokeResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Error "Token revocation failed: $($_.Exception.Message)"
}

# Test 7: Validate Revoked Token (should fail)
Write-Header "7. Testing Validation of Revoked Token"
Write-Host "POST $BaseUrl/validate (should fail)"

try {
    $ValidateRevokedResponse = Invoke-RestMethod -Uri "$BaseUrl/validate" -Method Post -Body $ValidateData -ContentType $ContentType
    
    if ($ValidateRevokedResponse.data.valid -eq $false) {
        Write-Success "Revoked token validation correctly failed"
        $RevokedMessage = $ValidateRevokedResponse.data.message
        Write-Info "Validation message: $RevokedMessage"
    } else {
        Write-Error "Revoked token validation incorrectly passed"
    }
    
    Write-Host "Response:"
    $ValidateRevokedResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Error "Revoked token validation request failed: $($_.Exception.Message)"
}

Write-Header "Test Summary"
Write-Info "All JWT API endpoint tests completed"
Write-Info "Check the results above for any failures"

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  Testing Complete" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
