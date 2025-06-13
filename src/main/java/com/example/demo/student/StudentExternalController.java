package com.example.demo.student;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for student-related external API integrations
 */
@RestController
@RequestMapping("/api/v1/student/external")
@CrossOrigin(origins = "*")
@Tag(name = "Student External API", description = "Student data enrichment with external APIs")
@SecurityRequirement(name = "bearerAuth")
public class StudentExternalController {

    @Autowired
    private StudentExternalApiService studentExternalApiService;

    /**
     * Get enriched student data with external API information
     */
    @GetMapping("/enriched/{studentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get Enriched Student Data", 
               description = "Fetch student data enriched with external API information")
    public ResponseEntity<Map<String, Object>> getEnrichedStudentData(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        Map<String, Object> enrichedData = studentExternalApiService.getEnrichedStudentData(studentId);
        return ResponseEntity.ok(enrichedData);
    }

    /**
     * Validate student email using external service
     */
    @GetMapping("/validate-email")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Validate Student Email", 
               description = "Validate email address using external validation service")
    public ResponseEntity<Map<String, Object>> validateEmail(
            @Parameter(description = "Email address to validate") @RequestParam String email) {
        Map<String, Object> validationResult = studentExternalApiService.validateStudentEmail(email);
        return ResponseEntity.ok(validationResult);
    }

    /**
     * Get university information
     */
    @GetMapping("/university")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get University Information", 
               description = "Fetch university information from external API")
    public ResponseEntity<Map<String, Object>> getUniversityInfo(
            @Parameter(description = "University name") @RequestParam String name) {
        Map<String, Object> universityInfo = studentExternalApiService.getUniversityInfo(name);
        return ResponseEntity.ok(universityInfo);
    }

    /**
     * Get educational resources for a subject
     */
    @GetMapping("/resources")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get Educational Resources", 
               description = "Fetch educational resources for a specific subject")
    public ResponseEntity<Map<String, Object>> getEducationalResources(
            @Parameter(description = "Subject to search for") @RequestParam String subject) {
        Map<String, Object> resources = studentExternalApiService.getEducationalResources(subject);
        return ResponseEntity.ok(resources);
    }

    /**
     * Get country information
     */
    @GetMapping("/country/{countryCode}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get Country Information", 
               description = "Fetch country information based on country code")
    public ResponseEntity<Map<String, Object>> getCountryInfo(
            @Parameter(description = "Country code (e.g., US, GB, FR)") @PathVariable String countryCode) {
        Map<String, Object> countryInfo = studentExternalApiService.getCountryInfo(countryCode);
        return ResponseEntity.ok(countryInfo);
    }

    /**
     * Batch enrich multiple students
     */
    @GetMapping("/batch-enrich")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Batch Enrich Students", 
               description = "Enrich multiple students with external data")
    public ResponseEntity<Map<String, Object>> batchEnrichStudents(
            @Parameter(description = "Number of students to enrich") 
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> enrichedStudents = studentExternalApiService.batchEnrichStudents(limit);
        return ResponseEntity.ok(enrichedStudents);
    }

    /**
     * Calculate student age with external API
     */
    @GetMapping("/age-calculation/{studentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Calculate Age with External API", 
               description = "Calculate student age using external date/time API")
    public ResponseEntity<Map<String, Object>> calculateAge(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        Student student = new Student();
        student.setId(studentId);
        // In real implementation, fetch student from repository
        Map<String, Object> ageData = studentExternalApiService.calculateAgeWithExternalApi(student);
        return ResponseEntity.ok(ageData);
    }
}