package com.example.demo.student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    public void addNewStudent(Student student) {
        Optional<Student> studentOptional = studentRepository
                .findStudentByEmail(student.getEmail());
        if (studentOptional.isPresent()) {
            throw new IllegalStateException("email taken");
        }
        studentRepository.save(student);
        System.out.println(student);
    }

    public void deleteStudent(Long studentId) {
        boolean exists = studentRepository.existsById(studentId);
        if (!exists) {
            throw new IllegalStateException("student with id " + studentId + " does not exist");
        }
        studentRepository.deleteById(studentId);
    }

    public Optional<Student> getStudentById(Long studentId) {
        return studentRepository.findById(studentId);
    }

    @Transactional
    public void updateStudent(Long studentId, String name, String email) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException(
                        "student with id " + studentId + " does not exist"));

        if (name != null && !name.isEmpty() && !name.equals(student.getName())) {
            student.setName(name);
        }

        if (email != null && !email.isEmpty() && !email.equals(student.getEmail())) {
            Optional<Student> studentOptional = studentRepository.findStudentByEmail(email);
            if (studentOptional.isPresent()) {
                throw new IllegalStateException("email taken");
            }
            student.setEmail(email);
        }
    }

    public List<Student> searchStudents(String name, String email, Integer minAge, Integer maxAge,
                                       int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        Page<Student> studentPage = studentRepository.findStudentsWithFilters(name, email, pageable);
        List<Student> students = studentPage.getContent();

        // Apply age filtering if specified
        if (minAge != null || maxAge != null) {
            students = students.stream()
                .filter(student -> {
                    int age = student.getAge();
                    return (minAge == null || age >= minAge) && (maxAge == null || age <= maxAge);
                })
                .collect(Collectors.toList());
        }

        return students;
    }    public List<Student> getStudentsByAgeRange(int minAge, int maxAge) {
        if (minAge > maxAge) {
            throw new IllegalArgumentException("Minimum age cannot be greater than maximum age");
        }
        // Since we removed the database query, we'll filter in Java
        return studentRepository.findAll().stream()
            .filter(student -> {
                int age = student.getAge();
                return age >= minAge && age <= maxAge;
            })
            .collect(Collectors.toList());
    }

    public Map<String, Object> getStudentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalStudents = studentRepository.countAllStudents();
        
        // Calculate average age in Java
        List<Student> allStudents = studentRepository.findAll();
        double averageAge = allStudents.stream()
            .mapToInt(Student::getAge)
            .average()
            .orElse(0.0);
        
        // Group students by age ranges
        Map<String, Long> ageGroups = allStudents.stream()
            .collect(Collectors.groupingBy(
                student -> {
                    int age = student.getAge();
                    if (age < 20) return "Under 20";
                    else if (age < 30) return "20-29";
                    else if (age < 40) return "30-39";
                    else return "40+";
                },
                Collectors.counting()
            ));
        
        stats.put("totalStudents", totalStudents);
        stats.put("averageAge", Math.round(averageAge * 100.0) / 100.0);
        stats.put("ageDistribution", ageGroups);
        stats.put("generatedAt", LocalDateTime.now());
        
        return stats;
    }

    public List<Student> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return studentRepository.findByKeyword(keyword.trim());
    }

    public List<Student> getStudentsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return studentRepository.findStudentsByDateOfBirthRange(startDate, endDate);
    }

    public List<Student> getStudentsByBirthYear(int year) {
        if (year < 1900 || year > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Invalid birth year");
        }
        return studentRepository.findStudentsByBirthYear(year);
    }

    public long getStudentCount() {
        return studentRepository.countAllStudents();
    }

    public List<Student> getStudentsOlderThan(int age) {
        return studentRepository.findAll().stream()
            .filter(student -> student.getAge() > age)
            .collect(Collectors.toList());
    }

    public List<Student> getStudentsYoungerThan(int age) {
        return studentRepository.findAll().stream()
            .filter(student -> student.getAge() < age)
            .collect(Collectors.toList());
    }
}