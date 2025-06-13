package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ExternalApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for external API integration
 */
@RestController
@RequestMapping("/api/v1/external")
@CrossOrigin(origins = "*")
@Tag(name = "External API", description = "Endpoints for calling external APIs")
@SecurityRequirement(name = "bearerAuth")
public class ExternalApiController {

    @Autowired
    private ExternalApiService externalApiService;

    /**
     * Generic endpoint to call any external API
     */
    @PostMapping("/call")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Call External API", description = "Make a request to any external API")
    public ResponseEntity<Map<String, Object>> callExternalApi(@RequestBody ExternalApiRequest request) {
        Map<String, Object> response = externalApiService.callExternalApi(
                request.getUrl(),
                HttpMethod.valueOf(request.getMethod()),
                request.getBody(),
                request.getHeaders()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get a random joke from external API
     */
    @GetMapping("/joke")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get Random Joke", description = "Fetch a random joke from external joke API")
    public ResponseEntity<Map<String, Object>> getRandomJoke() {
        Map<String, Object> response = externalApiService.getRandomJoke();
        return ResponseEntity.ok(response);
    }

    /**
     * Get user from JSONPlaceholder API
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get External User", description = "Fetch user data from JSONPlaceholder API")
    public ResponseEntity<Map<String, Object>> getExternalUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        Map<String, Object> response = externalApiService.getJsonPlaceholderUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a test post on JSONPlaceholder
     */
    @PostMapping("/create-post")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create External Post", description = "Create a test post on JSONPlaceholder API")
    public ResponseEntity<Map<String, Object>> createExternalPost(@RequestBody CreatePostRequest request) {
        Map<String, Object> response = externalApiService.createJsonPlaceholderPost(
                request.getTitle(),
                request.getBody(),
                request.getUserId()
        );
        return ResponseEntity.ok(response);    }

    /**
     * Health check for external API service
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if external API service is working")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "External API Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Request DTO for generic external API calls
     */
    public static class ExternalApiRequest {
        private String url;
        private String method = "GET";
        private Map<String, String> headers;
        private Object body;

        // Getters and setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
        }
    }

    /**
     * Request DTO for creating posts
     */
    public static class CreatePostRequest {
        private String title;
        private String body;
        private Long userId;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}