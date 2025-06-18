#!/bin/bash

# Centralized JWT API Testing Script
# This script tests all the JWT management endpoints

BASE_URL="http://localhost:8080/api/v1/jwt"
CONTENT_TYPE="Content-Type: application/json"

echo "========================================"
echo "  Centralized JWT API Testing Script"
echo "========================================"
echo ""

# Function to print colored output
print_success() {
    echo -e "\033[32m✓ $1\033[0m"
}

print_error() {
    echo -e "\033[31m✗ $1\033[0m"
}

print_info() {
    echo -e "\033[34mℹ $1\033[0m"
}

print_header() {
    echo ""
    echo -e "\033[33m$1\033[0m"
    echo "----------------------------------------"
}

# Test 1: Check Service Status
print_header "1. Testing Service Status"
echo "GET $BASE_URL/status"
STATUS_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/jwt_status.json "$BASE_URL/status")
STATUS_CODE="${STATUS_RESPONSE: -3}"

if [ "$STATUS_CODE" = "200" ]; then
    print_success "Service status check passed"
    echo "Response:"
    cat /tmp/jwt_status.json | jq . 2>/dev/null || cat /tmp/jwt_status.json
else
    print_error "Service status check failed (HTTP $STATUS_CODE)"
fi

# Test 2: Generate JWT Token (Login)
print_header "2. Testing Token Generation (Login)"
LOGIN_DATA='{
    "username": "admin",
    "password": "admin123"
}'

echo "POST $BASE_URL/generate"
echo "Data: $LOGIN_DATA"

LOGIN_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/jwt_login.json \
    -X POST \
    -H "$CONTENT_TYPE" \
    -d "$LOGIN_DATA" \
    "$BASE_URL/generate")

LOGIN_CODE="${LOGIN_RESPONSE: -3}"

if [ "$LOGIN_CODE" = "200" ]; then
    print_success "Token generation passed"
    JWT_TOKEN=$(cat /tmp/jwt_login.json | jq -r '.data.token' 2>/dev/null)
    if [ "$JWT_TOKEN" != "null" ] && [ -n "$JWT_TOKEN" ]; then
        print_info "JWT Token obtained: ${JWT_TOKEN:0:50}..."
        echo "Full Response:"
        cat /tmp/jwt_login.json | jq . 2>/dev/null || cat /tmp/jwt_login.json
    else
        print_error "No JWT token in response"
        JWT_TOKEN=""
    fi
else
    print_error "Token generation failed (HTTP $LOGIN_CODE)"
    cat /tmp/jwt_login.json
    JWT_TOKEN=""
fi

# Only continue with other tests if we have a valid token
if [ -z "$JWT_TOKEN" ]; then
    print_error "Cannot continue without valid JWT token"
    exit 1
fi

# Test 3: Validate Token (POST method)
print_header "3. Testing Token Validation (POST)"
VALIDATE_DATA="{
    \"token\": \"$JWT_TOKEN\",
    \"username\": \"admin\"
}"

echo "POST $BASE_URL/validate"
echo "Data: $VALIDATE_DATA"

VALIDATE_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/jwt_validate.json \
    -X POST \
    -H "$CONTENT_TYPE" \
    -d "$VALIDATE_DATA" \
    "$BASE_URL/validate")

VALIDATE_CODE="${VALIDATE_RESPONSE: -3}"

if [ "$VALIDATE_CODE" = "200" ]; then
    print_success "Token validation (POST) passed"
    IS_VALID=$(cat /tmp/jwt_validate.json | jq -r '.data.valid' 2>/dev/null)
    if [ "$IS_VALID" = "true" ]; then
        print_success "Token is valid"
    else
        print_error "Token validation returned false"
    fi
    echo "Response:"
    cat /tmp/jwt_validate.json | jq . 2>/dev/null || cat /tmp/jwt_validate.json
else
    print_error "Token validation (POST) failed (HTTP $VALIDATE_CODE)"
    cat /tmp/jwt_validate.json
fi

# Test 4: Validate Token (GET method with header)
print_header "4. Testing Token Validation (GET with Authorization header)"
echo "GET $BASE_URL/validate"
echo "Authorization: Bearer $JWT_TOKEN"

VALIDATE_GET_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/jwt_validate_get.json \
    -X GET \
    -H "Authorization: Bearer $JWT_TOKEN" \
    "$BASE_URL/validate")

VALIDATE_GET_CODE="${VALIDATE_GET_RESPONSE: -3}"

if [ "$VALIDATE_GET_CODE" = "200" ]; then
    print_success "Token validation (GET) passed"
    IS_VALID_GET=$(cat /tmp/jwt_validate_get.json | jq -r '.data.valid' 2>/dev/null)
    if [ "$IS_VALID_GET" = "true" ]; then
        print_success "Token is valid via header"
    else
        print_error "Token validation via header returned false"
    fi
    echo "Response:"
    cat /tmp/jwt_validate_get.json | jq . 2>/dev/null || cat /tmp/jwt_validate_get.json
else
    print_error "Token validation (GET) failed (HTTP $VALIDATE_GET_CODE)"
    cat /tmp/jwt_validate_get.json
fi

# Test 5: User Token Statistics
print_header "5. Testing User Token Statistics"
echo "GET $BASE_URL/user/admin/stats"

STATS_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/jwt_stats.json \
    "$BASE_URL/user/admin/stats")

STATS_CODE="${STATS_RESPONSE: -3}"

if [ "$STATS_CODE" = "200" ]; then
    print_success "User token statistics passed"
    TOKEN_COUNT=$(cat /tmp/jwt_stats.json | jq -r '.data.activeTokenCount' 2>/dev/null)
    print_info "Active tokens for user 'admin': $TOKEN_COUNT"
    echo "Response:"
    cat /tmp/jwt_stats.json | jq . 2>/dev/null || cat /tmp/jwt_stats.json
else
    print_error "User token statistics failed (HTTP $STATS_CODE)"
    cat /tmp/jwt_stats.json
fi

# Test 6: Revoke Token
print_header "6. Testing Token Revocation"
REVOKE_DATA="{
    \"token\": \"$JWT_TOKEN\",
    \"reason\": \"API testing\",
    \"revokeAllUserTokens\": false
}"

echo "POST $BASE_URL/revoke"
echo "Data: $REVOKE_DATA"

REVOKE_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/jwt_revoke.json \
    -X POST \
    -H "$CONTENT_TYPE" \
    -d "$REVOKE_DATA" \
    "$BASE_URL/revoke")

REVOKE_CODE="${REVOKE_RESPONSE: -3}"

if [ "$REVOKE_CODE" = "200" ]; then
    print_success "Token revocation passed"
    IS_REVOKED=$(cat /tmp/jwt_revoke.json | jq -r '.data.revoked' 2>/dev/null)
    if [ "$IS_REVOKED" = "true" ]; then
        print_success "Token successfully revoked"
    else
        print_error "Token revocation returned false"
    fi
    echo "Response:"
    cat /tmp/jwt_revoke.json | jq . 2>/dev/null || cat /tmp/jwt_revoke.json
else
    print_error "Token revocation failed (HTTP $REVOKE_CODE)"
    cat /tmp/jwt_revoke.json
fi

# Test 7: Validate Revoked Token (should fail)
print_header "7. Testing Validation of Revoked Token"
echo "POST $BASE_URL/validate (should fail)"

VALIDATE_REVOKED_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/jwt_validate_revoked.json \
    -X POST \
    -H "$CONTENT_TYPE" \
    -d "$VALIDATE_DATA" \
    "$BASE_URL/validate")

VALIDATE_REVOKED_CODE="${VALIDATE_REVOKED_RESPONSE: -3}"

if [ "$VALIDATE_REVOKED_CODE" = "200" ]; then
    IS_VALID_REVOKED=$(cat /tmp/jwt_validate_revoked.json | jq -r '.data.valid' 2>/dev/null)
    if [ "$IS_VALID_REVOKED" = "false" ]; then
        print_success "Revoked token validation correctly failed"
        REVOKED_MESSAGE=$(cat /tmp/jwt_validate_revoked.json | jq -r '.data.message' 2>/dev/null)
        print_info "Validation message: $REVOKED_MESSAGE"
    else
        print_error "Revoked token validation incorrectly passed"
    fi
    echo "Response:"
    cat /tmp/jwt_validate_revoked.json | jq . 2>/dev/null || cat /tmp/jwt_validate_revoked.json
else
    print_error "Revoked token validation request failed (HTTP $VALIDATE_REVOKED_CODE)"
    cat /tmp/jwt_validate_revoked.json
fi

# Cleanup temporary files
print_header "Cleanup"
rm -f /tmp/jwt_*.json
print_info "Temporary files cleaned up"

print_header "Test Summary"
print_info "All JWT API endpoint tests completed"
print_info "Check the results above for any failures"

echo ""
echo "========================================"
echo "  Testing Complete"
echo "========================================"
