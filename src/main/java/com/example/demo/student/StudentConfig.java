package com.example.demo.student;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

//@Configuration
//public class StudentConfig {
//    @Bean
//    CommandLineRunner commandLineRunner(StudentRepository studentRepository) {
//        return args -> {
//            Student Ray = new Student("Ray", "Rayen@gmail.com", LocalDate.of(2000, Month.SEPTEMBER, 5));
//            Student May = new Student("May", "Rayen111@gmail.com", LocalDate.of(2001, Month.SEPTEMBER, 5));
//            studentRepository.saveAll(List.of(Ray, May));
//        };
//    }
//}