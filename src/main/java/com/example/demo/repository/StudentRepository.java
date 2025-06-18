package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("SELECT s FROM Student s WHERE s.email = ?1")
    Optional<Student> findStudentByEmail(String email);
    
    @Query("SELECT s FROM Student s WHERE " +
           "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Student> findStudentsWithFilters(@Param("name") String name, 
                                         @Param("email") String email, 
                                         Pageable pageable);
      @Query("SELECT s FROM Student s WHERE s.dob BETWEEN :startDate AND :endDate")
    List<Student> findStudentsByDateOfBirthRange(@Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(s) FROM Student s")
    long countAllStudents();
      @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Student> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT s FROM Student s WHERE EXTRACT(YEAR FROM s.dob) = :year")
    List<Student> findStudentsByBirthYear(@Param("year") int year);
}
