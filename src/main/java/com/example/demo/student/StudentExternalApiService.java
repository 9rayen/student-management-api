package com.example.demo.student;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.service.ExternalApiService;

/**
 * Service to enrich student data with external API calls
 */
@Service
public class StudentExternalApiService {

    @Autowired
    private ExternalApiService externalApiService;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Get student data enriched with external API data
     * This example fetches additional data from JSONPlaceholder API
     */
    public Map<String, Object> getEnrichedStudentData(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        // Get student from database
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("Student not found"));
        
        // Add student data
        result.put("student", student);
        
        // Call external API to get additional data
        // Using the student ID as external user ID (just for demonstration)
        Map<String, Object> externalData = externalApiService.getJsonPlaceholderUser(studentId);
        
        // Add external data if successful
        if (externalData.get("data") != null) {
            result.put("externalProfile", externalData.get("data"));
        }
        
        // Get a random fact about the student (using joke API as example)
        Map<String, Object> randomFact = externalApiService.getRandomJoke();
        if (randomFact.get("data") != null) {
            result.put("funFact", randomFact.get("data"));
        }
        
        return result;
    }

    /**
     * Get university information for a student (mock example)
     * In a real scenario, this could call a university API
     */
    public Map<String, Object> getUniversityInfo(String universityName) {
        // This is a mock example - in reality, you would call a real university API
        String url = "https://universities.hipolabs.com/search";
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("name", universityName);
        queryParams.put("country", "United States");
        
        return externalApiService.getFromExternalApi(url, null, queryParams);
    }

    /**
     * Validate email using external API
     * This could use a real email validation service
     */
    public Map<String, Object> validateStudentEmail(String email) {
        // Example using a mock API - replace with real email validation service
        Map<String, Object> result = new HashMap<>();
        
        // For demonstration, we'll just check if email exists in our system
        boolean exists = studentRepository.findStudentByEmail(email).isPresent();
        
        result.put("email", email);
        result.put("valid", email.contains("@") && email.contains("."));
        result.put("existsInSystem", exists);
        result.put("timestamp", System.currentTimeMillis());
        
        // In a real scenario, you might call an email validation API like:
        // String url = "https://api.emailvalidation.com/validate";
        // return externalApiService.getFromExternalApi(url, headers, params);
        
        return result;
    }

    /**
     * Get country information based on student data
     * This could be based on phone number, address, etc.
     */
    public Map<String, Object> getCountryInfo(String countryCode) {
        String url = "https://restcountries.com/v3.1/alpha/" + countryCode;
        return externalApiService.getFromExternalApi(url, null, null);
    }

    /**
     * Get educational resources for a student
     * This example uses a public API
     */
    public Map<String, Object> getEducationalResources(String subject) {
        // Example using Open Library API
        String url = "https://openlibrary.org/search.json";
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", subject);
        queryParams.put("limit", "5");
        
        return externalApiService.getFromExternalApi(url, null, queryParams);
    }

    /**
     * Calculate student age using an external date API (demonstration)
     */
    public Map<String, Object> calculateAgeWithExternalApi(Student student) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("studentId", student.getId());
        result.put("name", student.getName());
        result.put("dateOfBirth", student.getDob());
        result.put("calculatedAge", student.getAge());
        
        // You could call a date/time API here for more complex calculations
        // For example, to get timezone-aware age calculations
        
        return result;
    }

    /**
     * Batch enrich multiple students with external data
     */
    public Map<String, Object> batchEnrichStudents(int limit) {
        Map<String, Object> result = new HashMap<>();
        
        // Get students
        var students = studentRepository.findAll().stream()
                .limit(limit)
                .map(student -> {
                    Map<String, Object> enrichedData = new HashMap<>();
                    enrichedData.put("id", student.getId());
                    enrichedData.put("name", student.getName());
                    enrichedData.put("email", student.getEmail());
                    enrichedData.put("age", student.getAge());
                    
                    // Add external data (simplified for performance)
                    // In production, consider caching or async processing
                    try {
                        var externalUser = externalApiService.getJsonPlaceholderUser(student.getId());
                        if (externalUser.get("data") != null) {
                            enrichedData.put("externalData", externalUser.get("data"));
                        }
                    } catch (Exception e) {
                        enrichedData.put("externalDataError", e.getMessage());
                    }
                    
                    return enrichedData;
                })
                .toList();
        
        result.put("enrichedStudents", students);
        result.put("count", students.size());
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}