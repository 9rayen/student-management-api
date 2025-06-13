package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service class for making external API calls
 */
@Service
public class ExternalApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Make a GET request to an external API
     * 
     * @param url The URL of the external API
     * @param headers Optional headers
     * @param queryParams Optional query parameters
     * @return Response as a Map
     */
    public Map<String, Object> getFromExternalApi(String url, Map<String, String> headers, Map<String, String> queryParams) {
        try {
            // Build URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            if (queryParams != null) {
                queryParams.forEach(builder::queryParam);
            }

            // Set headers
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

            // Make the request
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Parse response
            return parseResponse(response);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("status", e.getStatusCode().value());
            errorResponse.put("message", e.getMessage());
            errorResponse.put("body", e.getResponseBodyAsString());
            return errorResponse;
        } catch (ResourceAccessException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Connection timeout or network error: " + e.getMessage());
            return errorResponse;
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Invalid URL or parameters: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Failed to call external API: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Make a POST request to an external API
     * 
     * @param url The URL of the external API
     * @param requestBody The request body
     * @param headers Optional headers
     * @return Response as a Map
     */
    public Map<String, Object> postToExternalApi(String url, Object requestBody, Map<String, String> headers) {
        try {
            // Set headers
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            HttpEntity<Object> entity = new HttpEntity<>(requestBody, httpHeaders);

            // Make the request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parse response
            return parseResponse(response);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("status", e.getStatusCode().value());
            errorResponse.put("message", e.getMessage());
            errorResponse.put("body", e.getResponseBodyAsString());
            return errorResponse;
        } catch (ResourceAccessException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Connection timeout or network error: " + e.getMessage());
            return errorResponse;
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Invalid URL or request data: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Failed to call external API: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Make a generic HTTP request to an external API
     * 
     * @param url The URL of the external API
     * @param method The HTTP method
     * @param requestBody Optional request body
     * @param headers Optional headers
     * @return Response as a Map
     */
    public Map<String, Object> callExternalApi(String url, HttpMethod method, Object requestBody, Map<String, String> headers) {
        try {
            // Set headers
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            HttpEntity<Object> entity = new HttpEntity<>(requestBody, httpHeaders);

            // Make the request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    String.class
            );

            // Parse response
            return parseResponse(response);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("status", e.getStatusCode().value());
            errorResponse.put("message", e.getMessage());
            errorResponse.put("body", e.getResponseBodyAsString());
            return errorResponse;
        } catch (ResourceAccessException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Connection timeout or network error: " + e.getMessage());
            return errorResponse;
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Invalid URL, method, or request data: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Failed to call external API: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Parse response from external API
     */
    private Map<String, Object> parseResponse(ResponseEntity<String> response) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.getStatusCode().value());
        result.put("headers", response.getHeaders().toSingleValueMap());
        try {
            // Try to parse as JSON
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            result.put("data", objectMapper.convertValue(jsonNode, Map.class));
        } catch (com.fasterxml.jackson.core.JsonProcessingException | IllegalArgumentException e) {
            // If not JSON or conversion fails, return as string
            result.put("data", response.getBody());
        }
        
        return result;
    }

    /**
     * Call a public API example (no authentication required)
     */
    public Map<String, Object> getRandomJoke() {
        String url = "https://official-joke-api.appspot.com/random_joke";
        return getFromExternalApi(url, null, null);
    }

    /**
     * Call JSONPlaceholder API example
     */
    public Map<String, Object> getJsonPlaceholderUser(Long userId) {
        String url = "https://jsonplaceholder.typicode.com/users/" + userId;
        return getFromExternalApi(url, null, null);
    }

    /**
     * Create a post on JSONPlaceholder (test API)
     */
    public Map<String, Object> createJsonPlaceholderPost(String title, String body, Long userId) {
        String url = "https://jsonplaceholder.typicode.com/posts";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", title);
        requestBody.put("body", body);
        requestBody.put("userId", userId);
        
        return postToExternalApi(url, requestBody, null);
    }
}